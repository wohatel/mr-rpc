package com.murong.rpc.config;

import com.murong.rpc.interact.MrRequestInterceptor;
import com.murong.rpc.interact.MrServerAroundAdvice;
import com.murong.rpc.interact.ProceedJoinPoint;
import com.murong.rpc.util.MrTemplateFactory;
import com.murong.rpc.util.RpcConnector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * description
 *
 * @author yaochuang 2024/04/12 13:52
 */
@Configuration
@ComponentScan(basePackages = "com.murong.rpc.config")
public class BeanConfig {

    @Bean
    public RpcConnector rpcConnector(TemplateConfig templateConfig, MrRequestInterceptor mrRequestInterceptor) {
        MrTemplateFactory mrTemplateFactory = new MrTemplateFactory();
        RestTemplate rpcRestTemplate = mrTemplateFactory.createRpcRestTemplate(templateConfig);
        RestTemplate streamRestTemplate = mrTemplateFactory.createStreamRestTemplate(templateConfig);
        // 放入这个容器
        return new RpcConnector(rpcRestTemplate, streamRestTemplate, mrRequestInterceptor, templateConfig.getCacheDir());
    }

    @Bean
    @ConditionalOnMissingBean(MrServerAroundAdvice.class)
    public MrServerAroundAdvice simpleMrServerRoundAdvice() {
        // 定义一个MrInterceptor, 可以由客户端提供具体逻辑
        return ProceedJoinPoint::proceed;
    }

    @Bean
    @ConditionalOnMissingBean(MrRequestInterceptor.class)
    public MrRequestInterceptor simpleMrRequestInterceptor() {
        return (r, p) -> p.proceed();
    }


}
