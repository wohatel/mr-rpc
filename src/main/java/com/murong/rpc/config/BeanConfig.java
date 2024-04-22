package com.murong.rpc.config;

import com.murong.rpc.interact.MrRequestInterceptor;
import com.murong.rpc.interact.MrServerAroundAdvice;
import com.murong.rpc.interact.ProceedJoinPoint;
import com.murong.rpc.util.RpcConnector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * description
 *
 * @author yaochuang 2024/04/12 13:52
 */
@Configuration
public class BeanConfig {

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public RpcConnector rpcConnector(RestTemplate restTemplate, MrRequestInterceptor mrRpcInterceptor) {
        return new RpcConnector(restTemplate, mrRpcInterceptor);
    }

    @Bean
    @ConditionalOnMissingBean
    public MrRequestInterceptor mrRpcInterceptor() {
        // 定义一个MrInterceptor, 可以由客户端提供具体逻辑
        return headers -> {
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public MrServerAroundAdvice mrServerRoundAdvice() {
        // 定义一个MrInterceptor, 可以由客户端提供具体逻辑
        return ProceedJoinPoint::proceed;
    }


}
