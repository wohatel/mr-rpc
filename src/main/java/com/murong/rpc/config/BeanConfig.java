package com.murong.rpc.config;

import com.murong.rpc.interact.MrRequestInterceptor;
import com.murong.rpc.interact.MrRequestInterceptorChains;
import com.murong.rpc.interact.MrServerAroundAdvice;
import com.murong.rpc.interact.ProceedJoinPoint;
import com.murong.rpc.util.MrTemplateFactory;
import com.murong.rpc.util.RpcAttribute;
import com.murong.rpc.util.RpcConnector;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * description
 *
 * @author yaochuang 2024/04/12 13:52
 */
@Configuration
@ComponentScan(basePackages = "com.murong.rpc.config")
public class BeanConfig {

    @Bean
    public RpcConnector rpcConnector(TemplateConfig templateConfig, MrRequestInterceptorChains mrRequestInterceptorChains) {
        MrTemplateFactory mrTemplateFactory = new MrTemplateFactory();
        RestTemplate rpcRestTemplate = mrTemplateFactory.createRpcRestTemplate(templateConfig);
        RestTemplate streamRestTemplate = mrTemplateFactory.createStreamRestTemplate(templateConfig);
        // 放入这个容器
        final LinkedList<MrRequestInterceptor> list = new LinkedList<>();
        mrRequestInterceptorChains.interceptors(list);
        return new RpcConnector(rpcRestTemplate, streamRestTemplate, list);
    }

    @Bean
    @ConditionalOnMissingBean
    public MrRequestInterceptorChains mrRequestInterceptorChains() {
        // 定义一个MrInterceptor, 可以由客户端提供具体逻辑
        return list -> {
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public MrServerAroundAdvice mrServerRoundAdvice() {
        // 定义一个MrInterceptor, 可以由客户端提供具体逻辑
        return ProceedJoinPoint::proceed;
    }


}
