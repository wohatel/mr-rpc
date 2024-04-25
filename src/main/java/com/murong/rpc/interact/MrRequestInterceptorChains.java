package com.murong.rpc.interact;

import java.util.List;

/**
 * description
 *
 * @author yaochuang 2024/04/24 15:56
 */
public interface MrRequestInterceptorChains {
    /**
     * 客户端的拦截器链
     *
     * @param list 拦截器链
     */
    void interceptors(List<MrRequestInterceptor> list);
}
