package com.murong.rpc.interact;

import com.murong.rpc.util.Proceed;
import com.murong.rpc.util.RpcAttribute;

/**
 * description
 *
 * @author yaochuang 2024/04/24 15:56
 */
public interface MrRequestInterceptor {
    /**
     * @param rpcAttribute 请求属性
     * @param proceed      执行请求并相应结果
     */
    Object execute(RpcAttribute rpcAttribute, Proceed proceed);

}
