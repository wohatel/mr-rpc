package com.murong.rpc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 打在接口上,表示此类是mrRpc的客户端
 *
 * @author yaochuang 2024/04/11 09:29
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface MrClient {

    /**
     * 远程调用的地址
     *
     * @author yaochuang 2024-04-11 09:31
     */
    String url();
}
