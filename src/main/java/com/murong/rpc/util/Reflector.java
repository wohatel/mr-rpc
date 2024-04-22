package com.murong.rpc.util;

import com.alibaba.fastjson2.JSON;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * <p>
 * 反射工具类
 * </p>
 *
 * @author yaochuang 2024/04/15 13:56
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Reflector {

    /**
     * 执行实例方法
     *
     * @param method   方法本身
     * @param instance 实体
     * @param params   参数
     */
    @SneakyThrows
    public static Object execRealMethodWithParams(Method method, Object instance, Object[] params) {
        Object[] objects = transMethodWithParams(method, params);
        return method.invoke(instance, objects);
    }

    /**
     * 执行实例方法
     *
     * @param method 方法本身
     * @param params 参数
     */
    @SneakyThrows
    public static Object[] transMethodWithParams(Method method, Object[] params) {
        Type[] parameterTypes = method.getGenericParameterTypes();
        if (parameterTypes.length != params.length) {
            throw new IllegalArgumentException("参数数量不匹配");
        }
        // 将实际参数转换为方法需要的类型
        Object[] actualParams = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Type parameterType = parameterTypes[i];
            Object o = JSON.parseObject(JSON.toJSONString(params[i]), parameterType);
            actualParams[i] = o;
        }
        return actualParams;
    }

    @SneakyThrows
    public static Object execRealMethodWithReturnString(Method method, String returnString) {
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType == void.class) {
            return null;
        }
        return JSON.parseObject(returnString, genericReturnType);
    }

    /**
     * @param clazz  调用者
     * @param method 方法
     */
    public static String methodToString(Class<?> clazz, Method method) {
        String typeName = clazz.getTypeName();
        return typeName + "." + method.getName() + "." + JSON.toJSONString(method.getParameterTypes());
    }

    /**
     * 判断方法是否在父接口
     *
     * @param superInterface 接口
     * @param method         方法
     * @return boolean
     */
    public static boolean methodIsDeclaredInInterface(Class<?> superInterface, Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        // 如果是类本身
        if (superInterface == declaringClass) {
            return true;
        }
        // 获取所有的父接口
        Class<?>[] interfaces = declaringClass.getInterfaces();
        if (interfaces.length == 0) {
            return false;
        }
        boolean present = Arrays.stream(interfaces).filter(in -> in == superInterface).findFirst().stream().findFirst().isPresent();
        if (!present) {
            return false;
        }
        try {
            // 判断方法的是否在父类中有声明
            superInterface.getMethod(method.getName(), method.getParameterTypes());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
