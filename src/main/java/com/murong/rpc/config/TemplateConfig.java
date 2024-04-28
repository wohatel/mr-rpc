package com.murong.rpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置rpc的restTemplate
 *
 * @author yaochuang 2024/04/24 09:38
 */
@Component
@Data
@ConfigurationProperties(prefix = "mr.rpc.rest")
public class TemplateConfig {

    /**
     * 链接超时时间
     */
    private Integer connectTimeout = 5000;

    /**
     * 最大链接数
     */
    private Integer maxPoolSize = 100;

    /**
     * 从连接池获取连接的最长等待时间
     */
    private Integer connectionRequestTimeout = 5000;


    /**
     * 套接字超时时间
     */
    private Integer socketTimeout = 5000;

    /**
     * 每个目标主机最大连接数
     */
    private Integer maxPerRouteSize = 20;

    /**
     * rpc的缓存缓存目录
     */
    private String cacheDir;

}
