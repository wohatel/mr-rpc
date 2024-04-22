package com.murong.rpc.interact;

import org.springframework.http.HttpHeaders;

/**
 * 调用方拦截器
 *
 * @author yaochuang 2024/04/22 12:52
 */
public interface MrRequestInterceptor {
    void apply(HttpHeaders headers);
}