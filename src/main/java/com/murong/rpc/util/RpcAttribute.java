package com.murong.rpc.util;

import lombok.Data;
import org.springframework.http.HttpHeaders;

import java.lang.reflect.Method;

/**
 * description
 *
 * @author yaochuang 2024/04/24 16:12
 */
@Data
public class RpcAttribute {
    /**
     * 远程调用的端点
     */
    private String endpoint;

    /**
     * 调用的版本
     */
    private Integer version;

    /**
     * 调用具体方法
     */
    private String method;

    /**
     * 请求头对象
     */
    private HttpHeaders headers;

    /**
     * 请求的参数
     */
    private Object[] realParams;
}
