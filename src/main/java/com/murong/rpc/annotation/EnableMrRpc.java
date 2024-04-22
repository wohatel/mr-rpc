package com.murong.rpc.annotation;//package com.seabox.authen.annotation;

import com.murong.rpc.config.BeanConfig;

import com.murong.rpc.controller.MrRpcCommonController;
import com.murong.rpc.processor.MrClientPostProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用mrRpc
 *
 * @author yaochuang 2024/04/12 11:04
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({MrClientPostProcessor.class, BeanConfig.class, MrRpcCommonController.class})
public @interface EnableMrRpc {

}
