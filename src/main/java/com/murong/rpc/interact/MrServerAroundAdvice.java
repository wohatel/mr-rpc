package com.murong.rpc.interact;


import java.lang.reflect.InvocationTargetException;

/**
 * 调用方拦截器
 *
 * @author yaochuang 2024/04/22 12:52
 */
public interface MrServerAroundAdvice {

    /**
     * 切面通知
     *
     * @param joinPoint 切入点
     * @return object 响应结果
     * @throws InvocationTargetException 执行异常
     * @throws IllegalAccessException    参数异常
     */
    Object proceed(ProceedJoinPoint joinPoint) throws InvocationTargetException, IllegalAccessException;
}