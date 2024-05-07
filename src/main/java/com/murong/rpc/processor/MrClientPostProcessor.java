package com.murong.rpc.processor;

import com.murong.rpc.annotation.MrAutowired;
import com.murong.rpc.annotation.MrClient;
import com.murong.rpc.annotation.MrVersion;
import com.murong.rpc.cache.RpcCache;
import com.murong.rpc.exception.RpcExecption;
import com.murong.rpc.util.Reflector;
import com.murong.rpc.util.RpcConnector;
import com.murong.rpc.util.RpcRequest;
import com.murong.rpc.util.StreamUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Bean的后置处理器,处理消费者的监听解析
 * </p>
 *
 * @author yaochuang 2023/12/28 18:04
 */
@Component
@RequiredArgsConstructor
public class MrClientPostProcessor implements BeanPostProcessor {

    private final Environment environment;

    private final RpcConnector rpcConnector;


    @Nullable
    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        return bean;
    }

    @SneakyThrows
    @Nullable
    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        //序列化客户端
        settingClientField(bean);
        //缓存服务端
        settingServerInterface(bean);
        return bean;
    }

    private void settingServerInterface(Object bean) {
        Class<?> aClass = bean.getClass();
        Class<?>[] interfaces = aClass.getInterfaces();
        List<Class<?>> list = Arrays.stream(interfaces).filter(interfaceInstace -> interfaceInstace.isAnnotationPresent(MrClient.class)).toList();
        if (list.isEmpty()) {
            return;
        }
        if (list.size() > 1) {
            throw new RpcExecption(aClass.getName() + "实现了多个 @MrClient 接口是不被允许的");
        }
        Class<?> interfaceInstace = list.get(0);

        List<Method> noneStaticAndDefaultMethods = StreamUtil.getNoneStaticAndDefaultMethods(interfaceInstace);
        if (CollectionUtils.isEmpty(noneStaticAndDefaultMethods)) {
            return;
        }
        for (Method method : noneStaticAndDefaultMethods) {
            RpcCache.paddingInterfaceMethod(interfaceInstace, method, bean);
        }
        // 获取所有声明的方法
        Method[] declaredMethods = aClass.getDeclaredMethods();
        for (Method beanMethod : declaredMethods) {
            // 如果方法上打有注解
            if (beanMethod.isAnnotationPresent(MrVersion.class)) {
                MrVersion mrVersion = beanMethod.getDeclaredAnnotation(MrVersion.class);
                if (mrVersion.value() <= 0) {
                    throw new RpcExecption("方法版本必须大于0");
                }
                String name = beanMethod.getName();
                if (!name.endsWith("$" + mrVersion.value())) {
                    throw new RpcExecption("方法名需要满足[原方法名]$[mrVersion]格式,如 getUser$2() 表示getUser()的新版本");
                }

                RpcCache.paddingVersionMethod(interfaceInstace, beanMethod, bean, mrVersion);
            }
        }

    }

    @SneakyThrows
    public void settingClientField(Object bean) {
        List<Field> fieldList = StreamUtil.getLeafClientField(bean.getClass());
        for (Field field : fieldList) {
            ReflectionUtils.makeAccessible(field);
            Class<?> typeClass = field.getType();
            MrAutowired declaredAnnotation = field.getDeclaredAnnotation(MrAutowired.class);
            if (RpcCache.getCACHE_RPC_CLIENT().containsKey(typeClass)) {
                ReflectionUtils.setField(field, bean, RpcCache.getCACHE_RPC_CLIENT().get(typeClass));
            } else {
                List<Method> noneStaticAndDefaultMethods = StreamUtil.getNoneStaticAndDefaultMethods(typeClass);
                MrClient leafClient = typeClass.getAnnotation(MrClient.class);
                Object proxyObject = Proxy.newProxyInstance(typeClass.getClassLoader(), new Class[]{typeClass}, (proxy, method, objs) -> {
                    // 如果不是静态的话default方法
                    if (noneStaticAndDefaultMethods.contains(method)) {
                        return execMethod(typeClass, leafClient, method, objs, declaredAnnotation.version());
                    } else { // 如果是静态或者default方法
                        return method.invoke(proxy, objs);
                    }
                });
                ReflectionUtils.setField(field, bean, proxyObject);
                RpcCache.getCACHE_RPC_CLIENT().put(typeClass, proxyObject);
            }

        }
    }


    /**
     * 解析方法
     *
     * @param typeClass 调用方
     * @param rpcClient 注解
     * @param method    调用方法
     * @param objs      参数
     * @return Object   返回值
     */
    @SneakyThrows
    @SuppressWarnings("all")
    public Object execMethod(Class<?> typeClass, MrClient rpcClient, Method method, Object[] objs, int version) {
        String methodStr = Reflector.methodToString(typeClass, method);
        String url = rpcClient.url();
        if (url.startsWith("${") && url.endsWith("}")) {
            url = environment.getProperty(url.substring(2, url.length() - 1));
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
        }
        // 定义调用参数
        RpcRequest request = new RpcRequest();
        request.setMethod(methodStr);
        request.setVersion(version);

        if (StreamUtil.isUpload(method)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Assert.isTrue(StreamUtil.isSimpleUpload(parameterTypes), "请检查方法是否是标准的流上传:" + method);
            request.setParams(new Object[parameterTypes.length]);
            InputStream inputStream = null;
            for (int i = 0; i < objs.length; i++) {// 长度
                Class<?> parameterType = parameterTypes[i];
                request.getParams()[i] = objs[i]; // 复制实际参数
                if (parameterType == InputStream.class) {
                    request.setIndex(i);
                    inputStream = (InputStream) objs[i];
                    request.getParams()[i] = null; // 如果是流就设置为null
                }
            }
            return rpcConnector.upload(url, inputStream, method.getGenericReturnType(), request);
        } else if (StreamUtil.isDownload(method)) {
            request.setParams(objs);
            return rpcConnector.download(url, request);
        } else {
            request.setParams(objs);
            String result = rpcConnector.exchangeRpcType(url, String.class, request);
            return Reflector.execRealMethodWithReturnString(method, result);
        }
    }

}
