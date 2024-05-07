package com.murong.rpc.util;

import com.murong.rpc.config.TemplateConfig;
import io.netty.channel.ChannelOption;
import lombok.Getter;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;


/**
 * description
 *
 * @author yaochuang 2024/04/24 09:25
 */

@Getter
public class MrTemplateFactory {

    /**
     * 创建webClient
     */
    public WebClient createRpcWebClient(TemplateConfig templateConfig) {
        if (templateConfig == null) {
            templateConfig = new TemplateConfig();
        }
        final TemplateConfig current = templateConfig;

        ConnectionProvider connectionProvider = ConnectionProvider.builder("mrConnectionPool").maxConnections(templateConfig.getMaxPoolSize()).pendingAcquireTimeout(Duration.ofMillis(templateConfig.getConnectionRequestTimeout())).build();

        HttpClient httpClient = HttpClient.create(connectionProvider).tcpConfiguration(tcpClient -> tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, current.getConnectTimeout()).option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true));

        // 使用自定义的HttpClient创建ReactorClientHttpConnector
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        // 使用自定义的connector创建WebClient
        return WebClient.builder().clientConnector(connector).build();
    }
}
