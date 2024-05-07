package com.murong.rpc.util;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;

import java.lang.reflect.Method;

/**
 * description
 *
 * @author yaochuang 2024/04/24 16:12
 */
@Getter
public class RpcAttribute {

    public RpcAttribute(String endpoint, Integer version, String method) {
        this.endpoint = endpoint;
        this.version = version;
        this.method = method;
    }

    /**
     * 远程调用的端点
     */
    private final String endpoint;

    /**
     * 调用的版本
     */
    private final Integer version;

    /**
     * 调用具体方法
     */
    private final String method;

    /**
     * 请求头对象
     */
    @Setter
    private HttpHeaders headers;

    /**
     * 请求的参数
     */
    @Setter
    private Object[] realParams;
}
