package com.murong.rpc.controller;

import com.alibaba.fastjson2.JSON;
import com.murong.rpc.cache.RpcCache;
import com.murong.rpc.config.SpringContext;
import com.murong.rpc.util.RpcRequest;
import com.murong.rpc.constant.RpcUrl;
import com.murong.rpc.interact.MrServerAroundAdvice;
import com.murong.rpc.interact.ProceedJoinPoint;
import com.murong.rpc.util.DefaultKeyValue;
import com.murong.rpc.util.StreamUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.Charset;

import static com.murong.rpc.util.Reflector.transMethodWithParams;

/**
 * description
 *
 * @author yaochuang 2024/04/11 11:00
 */
@RestController
@Log
public class MrRpcCommonController {

    @Autowired
    SpringContext springContext;


    @SneakyThrows
    @PostMapping(value = RpcUrl.RPC)
    public String common(@RequestBody String param, HttpServletRequest httpServletRequest, HttpServletResponse response) {
        try {
            RpcRequest rpcRequest = JSON.parseObject(param, RpcRequest.class);
            DefaultKeyValue<Object, Method> objectMethodDefaultKeyValue = RpcCache.getVersionMethod(rpcRequest);
            Object instance = objectMethodDefaultKeyValue.getKey();
            Method method = objectMethodDefaultKeyValue.getValue();
            Object[] objects = transMethodWithParams(method, rpcRequest.getParams());
            // 服务端拦截执行逻辑
            Object proceed = proceedResponse(httpServletRequest, instance, method, objects);
            return JSON.toJSONString(proceed);
        } catch (Exception e) {
            log.warning(e.getMessage());
            response.setStatus(500);
            response.getWriter().write(e.getMessage());
        }
        return null;
    }

    @SneakyThrows
    @PostMapping(value = RpcUrl.HEART)
    public boolean heartBeat() {
        return true;
    }

    @SneakyThrows
    @PostMapping(value = RpcUrl.UPLOAD)
    public String upload(@RequestParam(name = "param") String param, HttpServletRequest httpServletRequest, HttpServletResponse response) {
        try {
            RpcRequest rpcRequest = JSON.parseObject(URLDecoder.decode(param, Charset.defaultCharset()), RpcRequest.class);
            DefaultKeyValue<Object, Method> objectMethodDefaultKeyValue = RpcCache.getVersionMethod(rpcRequest);
            Object instance = objectMethodDefaultKeyValue.getKey();
            Method method = objectMethodDefaultKeyValue.getValue();
            Object[] realParams = transMethodWithParams(method, rpcRequest.getParams());
            // 设置流
            realParams[rpcRequest.getIndex()] = httpServletRequest.getInputStream();
            Object proceed = proceedResponse(httpServletRequest, instance, method, realParams);
            return JSON.toJSONString(proceed);
        } catch (Exception e) {
            log.warning(e.getMessage());
            response.setStatus(500);
            response.getWriter().write(e.getMessage());
        }
        return null;
    }

    @SneakyThrows
    @PostMapping(value = RpcUrl.DOWNLOAD)
    public void download(@RequestBody String param, HttpServletRequest httpServletRequest, HttpServletResponse response) {
        try {
            RpcRequest rpcRequest = JSON.parseObject(param, RpcRequest.class);
            DefaultKeyValue<Object, Method> objectMethodDefaultKeyValue = RpcCache.getVersionMethod(rpcRequest);
            Object instance = objectMethodDefaultKeyValue.getKey();
            Method method = objectMethodDefaultKeyValue.getValue();
            Object[] realParams = transMethodWithParams(method, rpcRequest.getParams());

            InputStream inputStream = (InputStream) proceedResponse(httpServletRequest, instance, method, realParams);
            StreamUtil.inputStreamToOutputStream(inputStream, response.getOutputStream());
        } catch (Exception e) {
            log.warning(e.getMessage());
            response.setStatus(500);
            response.getWriter().write(e.getMessage());
        }
    }

    /**
     * 生成proceed对象调用
     *
     * @param instance 实例
     * @param method   方法
     * @param objects  实际参数
     */
    private Object proceedResponse(HttpServletRequest httpServletRequest, Object instance, Method method, Object[] objects) throws InvocationTargetException, IllegalAccessException {
        ProceedJoinPoint proceedJoinPoint = new ProceedJoinPoint() {

            @Override
            public String getHeader(String header) {
                return httpServletRequest.getHeader(header);
            }

            @Override
            public Object getInstance() {
                return instance;
            }

            @Override
            public Object getMethod() {
                return method;
            }

            @Override
            public Object[] getActualParams() {
                return objects;
            }

            @Override
            public Object proceed() throws InvocationTargetException, IllegalAccessException {
                return method.invoke(instance, objects);
            }
        };
        return springContext.getBean(MrServerAroundAdvice.class).proceed(proceedJoinPoint);
    }
}
