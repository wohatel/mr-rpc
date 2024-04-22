package com.murong.rpc.cache;

import com.alibaba.fastjson2.JSON;
import com.murong.rpc.annotation.MrVersion;
import com.murong.rpc.config.RpcRequest;
import com.murong.rpc.exception.RpcExecption;
import com.murong.rpc.util.Reflector;
import lombok.AccessLevel;
import lombok.Getter;
import com.murong.rpc.util.DefaultKeyValue;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description
 *
 * @author yaochuang 2024/04/11 09:50
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RpcCache {

    @Getter
    private static final Map<Class<?>, Object> CACHE_RPC_CLIENT = new ConcurrentHashMap<>();

    /**
     * 版本方法
     */
    @Getter
    private static final Map<String, Map<Integer, DefaultKeyValue<Object, Method>>> CACHE_SERVER_VERSION_METHOD = new ConcurrentHashMap<>();


    /**
     * 添加原始方法
     *
     * @param mrClientInterface mrClient方法
     * @param beanMethod        实例的beanMethod方法
     * @param object            实例本身
     */
    public static void paddingInterfaceMethod(Class<?> mrClientInterface, Method beanMethod, Object object) {
        String key = mrClientInterface.getTypeName() + "." + beanMethod.getName() + "." + JSON.toJSONString(beanMethod.getParameterTypes());
        Map<Integer, DefaultKeyValue<Object, Method>> integerDefaultKeyValueMap = CACHE_SERVER_VERSION_METHOD.get(key);
        DefaultKeyValue<Object, Method> defaultKeyValue = new DefaultKeyValue<>();
        defaultKeyValue.setKey(object);
        defaultKeyValue.setValue(beanMethod);
        if (integerDefaultKeyValueMap == null) {
            integerDefaultKeyValueMap = new HashMap<>(16);
            integerDefaultKeyValueMap.put(0, defaultKeyValue);
            CACHE_SERVER_VERSION_METHOD.put(key, integerDefaultKeyValueMap);
        } else {
            integerDefaultKeyValueMap.put(0, defaultKeyValue);
        }
    }

    /**
     * 添加version方法
     *
     * @param mrClientInterface mrClient方法
     * @param beanMethod        实例的beanMethod方法
     * @param object            实例本身
     * @param mrVersion         版本
     */
    public static void paddingVersionMethod(Class<?> mrClientInterface, Method beanMethod, Object object, MrVersion mrVersion) {
        String s = "$" + mrVersion.value();
        int i = beanMethod.getName().length() - s.length();
        String realMethodName = beanMethod.getName().substring(0, i);
        String key = mrClientInterface.getTypeName() + "." + realMethodName + "." + JSON.toJSONString(beanMethod.getParameterTypes());

        Map<Integer, DefaultKeyValue<Object, Method>> integerDefaultKeyValueMap = CACHE_SERVER_VERSION_METHOD.get(key);

        if (integerDefaultKeyValueMap == null) {
            throw new RpcExecption(object + "的方法名:" + beanMethod.getName() + "定义有误;方法名需要满足[原方法名]$[mrVersion]格式,如 getUser$2() 表示MrClient.getUser()的某个版本");
        }

        DefaultKeyValue<Object, Method> defaultKeyValue = new DefaultKeyValue<>();
        defaultKeyValue.setKey(object);
        defaultKeyValue.setValue(beanMethod);
        // 将新的值存入
        integerDefaultKeyValueMap.put(mrVersion.value(), defaultKeyValue);
    }


    /**
     * 添加version方法
     */
    public static DefaultKeyValue<Object, Method> getVersionMethod(String methodDeclare, int version) {
        Map<Integer, DefaultKeyValue<Object, Method>> integerDefaultKeyValueMap = CACHE_SERVER_VERSION_METHOD.get(methodDeclare);
        DefaultKeyValue<Object, Method> defaultKeyValue = integerDefaultKeyValueMap.get(version);
        if (defaultKeyValue != null) {
            return defaultKeyValue;
        }
        // 找指定版本范围内最大的
        Iterator<Map.Entry<Integer, DefaultKeyValue<Object, Method>>> iterator = integerDefaultKeyValueMap.entrySet().iterator();
        int currentVersion = 0;
        DefaultKeyValue<Object, Method> current = null;
        while (iterator.hasNext()) {
            Map.Entry<Integer, DefaultKeyValue<Object, Method>> next = iterator.next();
            Integer key = next.getKey();
            DefaultKeyValue<Object, Method> value = next.getValue();
            if (current == null) {
                current = value;
                currentVersion = key;
            } else {
                if (key > currentVersion && key < version) {
                    current = value;
                    currentVersion = key;
                }
            }

        }
        return current;

    }

    /**
     * 获取version方法
     */
    public static DefaultKeyValue<Object, Method> getVersionMethod(RpcRequest request) {
        return getVersionMethod(request.getMethod(), request.getVersion());
    }

}
