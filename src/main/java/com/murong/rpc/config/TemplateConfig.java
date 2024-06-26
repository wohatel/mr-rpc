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
     * dir缓存用户与文件存储
     */
    private String cacheDir;

}
