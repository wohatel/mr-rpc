package com.murong.rpc.interact;

import jakarta.servlet.http.Cookie;

import java.lang.reflect.InvocationTargetException;

/**
 * description
 *
 * @author yaochuang 2024/04/22 16:07
 */
public interface ProceedJoinPoint {

    /**
     * 获取header
     */
    String getHeader(String header);

    /**
     * 获取header
     */
    Cookie[] getCookies(String header);

    /**
     * 获取实例
     */
    Object getInstance();

    /**
     * 获取方法定义
     */
    Object getMethod();

    /**
     * 获取实际参数
     */
    Object[] getActualParams();

    /**
     * 执行方法
     */
    Object proceed() throws InvocationTargetException, IllegalAccessException;

}
