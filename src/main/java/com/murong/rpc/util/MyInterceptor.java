package com.murong.rpc.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import java.io.IOException;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;

public class MyInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // 在发送请求之前执行拦截操作
        System.out.println("Intercepting request: " + request.getURI());

        HttpHeaders headers = request.getHeaders();


        // 执行请求，并获取响应
        ClientHttpResponse response = execution.execute(request, body);

        // 在接收响应后执行拦截操作
        System.out.println("Intercepting response: " + response.getStatusCode());

        // 返回响应
        return response;
    }

    public static void main(String[] args) {
        // 创建 RestTemplate 实例
        RestTemplate restTemplate = new RestTemplate();

        // 创建自定义的拦截器
        MyInterceptor interceptor = new MyInterceptor();

        // 将拦截器注册到 RestTemplate 中
        restTemplate.setInterceptors(Collections.singletonList(interceptor));

        // 使用 RestTemplate 发送 HTTP 请求
        // ...
    }
}
