package com.murong.rpc.util;

import com.murong.rpc.config.TemplateConfig;
import lombok.Getter;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


/**
 * description
 *
 * @author yaochuang 2024/04/24 09:25
 */

@Getter
public class MrTemplateFactory {

    /**
     * 文件上传下载时使用的restTemplate,每次使用新链接
     *
     * @return RestTemplate
     */
    public RestTemplate createStreamRestTemplate(TemplateConfig templateConfig) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        // 对于文件上传下载,读取超时不限制
        requestFactory.setReadTimeout(0);
        // 设置连接超时
        requestFactory.setConnectTimeout(templateConfig.getConnectTimeout());
        return new RestTemplate(requestFactory);
    }

    /**
     * 普通rpc的restTemplate
     *
     * @return RestTemplate
     */
    public RestTemplate createRpcRestTemplate(TemplateConfig templateConfig) {
        if (templateConfig == null) {
            templateConfig = new TemplateConfig();
        }
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(templateConfig.getMaxPoolSize());
        connectionManager.setDefaultMaxPerRoute(templateConfig.getMaxPerRouteSize());
        RequestConfig.Builder custom = RequestConfig.custom();
        RequestConfig requestConfig = custom.setConnectionRequestTimeout(templateConfig.getConnectionRequestTimeout())
                .setSocketTimeout(templateConfig.getSocketTimeout())
                .setConnectTimeout(templateConfig.getConnectTimeout())
                .build();
        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig).build();
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }
}
