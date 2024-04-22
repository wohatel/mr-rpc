package com.murong.rpc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 打在field上,表示该类注入到spring容器
 *
 * @author yaochuang 2023/12/28 15:24
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface MrAutowired {
    int version() default Integer.MAX_VALUE;
}
