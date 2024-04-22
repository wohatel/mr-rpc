package com.murong.rpc.exception;

import lombok.Data;

/**
 * description
 *
 * @author yaochuang 2024/04/15 10:28
 */
@Data
public class RpcExecption extends RuntimeException {

    private Integer code = 500;

    private String msg;


    public RpcExecption() {
    }

    public RpcExecption(String msg) {
        super(msg);
    }

    public RpcExecption(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

}
