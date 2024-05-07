package com.murong.rpc.config;

import com.murong.rpc.util.RunnerUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author yaochuang
 */
@Component
public class SpringContext implements ApplicationContextAware {
    /**
     * 上下文
     */
    private static ApplicationContext context;

    /**
     * 日落应用上下文
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    /**
     * 得到bean
     */
    public static <T> T getBean(String beanName) {
        return (T) RunnerUtil.execSilentExceptionTo(() -> context.getBean(beanName), null);
    }

    /**
     * 得到bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return RunnerUtil.execSilentExceptionTo(() -> context.getBean(clazz), null);
    }

}
