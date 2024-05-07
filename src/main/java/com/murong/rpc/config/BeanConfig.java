package com.murong.rpc.config;

import com.murong.rpc.interact.MrRequestInterceptor;
import com.murong.rpc.util.MrTemplateFactory;
import com.murong.rpc.util.RpcConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * description
 *
 * @author yaochuang 2024/04/12 13:52
 */
@Configuration
@ComponentScan(basePackages = "com.murong.rpc.config")
public class BeanConfig {


    @Bean
    public RpcConnector rpcConnector(TemplateConfig templateConfig) {

        MrTemplateFactory mrTemplateFactory = new MrTemplateFactory();
        WebClient rpcWebClient = mrTemplateFactory.createRpcWebClient(templateConfig);
        // 放入这个容器
        return new RpcConnector(rpcWebClient,templateConfig.getCacheDir());
    }

}
