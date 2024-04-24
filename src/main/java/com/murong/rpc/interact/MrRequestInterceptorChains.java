package com.murong.rpc.interact;

import java.util.List;

/**
 * description
 *
 * @author yaochuang 2024/04/24 15:56
 */
public interface MrRequestInterceptorChains {
    void interceptors(List<MrRequestInterceptor> list);
}
