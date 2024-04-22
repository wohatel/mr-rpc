package com.murong.rpc.interact;


import java.lang.reflect.InvocationTargetException;

/**
 * 调用方拦截器
 *
 * @author yaochuang 2024/04/22 12:52
 */
public interface MrServerAroundAdvice {
    Object proceed(ProceedJoinPoint joinPoint) throws InvocationTargetException, IllegalAccessException;
}