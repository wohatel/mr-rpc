package com.murong.rpc.config;

import lombok.Data;

/**
 * description
 *
 * @author yaochuang 2024/04/12 15:20
 */
@Data
public class RpcRequest {

    /**
     * 方法的版本
     */
    private int version;

    /**
     * input流参数的位置
     */
    private int index = -1;

    /**
     * 方法名
     */
    private String method;


    /**
     * 实际参数
     */
    private Object[] params;
}
