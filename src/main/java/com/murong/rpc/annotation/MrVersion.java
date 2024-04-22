package com.murong.rpc.annotation;

import org.springframework.cglib.SpringCglibInfo;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MrRpc的版本
 *
 * @author yaochuang 2024/04/11 09:29
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface MrVersion {

    /**
     * 远程调用的地址
     *
     * @author yaochuang 2024-04-11 09:31
     */
    int value();

}
