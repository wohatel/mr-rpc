package com.murong.rpc.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.murong.rpc.config.SpringContext;
import com.murong.rpc.constant.RpcCodeEnum;
import com.murong.rpc.constant.RpcUrl;
import com.murong.rpc.exception.RpcExecption;
import com.murong.rpc.interact.MrRequestInterceptor;
import jakarta.json.Json;
import lombok.SneakyThrows;
import org.reactivestreams.Publisher;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * description
 *
 * @author yaochuang 2024/04/26 11:23
 */
public class RpcConnector {

    private final WebClient webClient;

    private final String cacheDir;


    private MrRequestInterceptor loadMrRequestInterceptor() {
        MrRequestInterceptor mrInterceptor = SpringContext.getBean(MrRequestInterceptor.class);
        return Objects.requireNonNullElseGet(mrInterceptor, () -> (r, p) -> p.proceed());
    }

    public RpcConnector(WebClient webClient, String cacheDir) {
        this.webClient = webClient;
        this.cacheDir = cacheDir;
    }


    /**
     * 返回单个对象
     *
     * @param url    请求url
     * @param tclass 返回类型
     * @param <T>    泛型t
     * @return 放回结果
     */
    public <T> T exchangeRpcType(String url, Class<T> tclass, RpcRequest rpcRequest) {
        HttpHeaders httpHeaders = new HttpHeaders();
        return execInterceptors(httpHeaders, url, rpcRequest, () -> {
            ClientResponse response = webClient.post().uri(url + RpcUrl.RPC).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).headers(headers -> headers.addAll(httpHeaders)).bodyValue(JSON.toJSONString(rpcRequest)).exchange().block();
            assert response != null;
            int value = response.statusCode().value();
            Mono<String> stringMono = response.bodyToMono(String.class);
            String block = stringMono.block();
            if (value == RpcCodeEnum.SUCCESS_CODE) {
                return JSONObject.parseObject(block, tclass);
            } else if (value == RpcCodeEnum.FAIL_CODE) {
                throw new RpcExecption(value, "rpc异常:" + block);
            } else {
                throw new RpcExecption(value, "未知异常:" + block);
            }
        });
    }

    /**
     * 返回单个对象
     *
     * @param url 请求url
     * @return 放回结果
     */
    public <T> T upload(String url, InputStream inputStream, Type genericReturnType, RpcRequest rpcRequest) {
        HttpHeaders httpHeaders = new HttpHeaders();
        return execInterceptors(httpHeaders, url, rpcRequest, () -> {
            String param = JSON.toJSONString(rpcRequest);
            String encode = URLEncoder.encode(param, Charset.defaultCharset());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url + RpcUrl.UPLOAD);
            builder.queryParam("param", encode);
            URI uri = builder.build().toUri();
            try {
                Flux<DataBuffer> body = DataBufferUtils.readInputStream(() -> inputStream,
                        // 设置缓冲区工厂，这里使用默认的工厂
                        DefaultDataBufferFactory.sharedInstance,
                        // 每次读取的最大字节数
                        1024);

                // 发送请求并获取响应
                ClientResponse response = webClient.post().uri(uri).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE).headers(headers -> headers.putAll(httpHeaders)).body(BodyInserters.fromDataBuffers(body)).exchange().block();

                assert response != null;
                int value = response.statusCode().value();
                Mono<String> stringMono = response.bodyToMono(String.class);
                String block = stringMono.block();
                if (value == RpcCodeEnum.SUCCESS_CODE) {
                    return JSONObject.parseObject(block, genericReturnType);
                } else if (value == RpcCodeEnum.FAIL_CODE) {
                    throw new RpcExecption(value, "rpc异常:" + block);
                } else {
                    throw new RpcExecption(value, "未知异常:" + block);
                }
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });

    }

    /**
     * 返回单个对象
     *
     * @param url 请求url
     * @return 放回结果
     */
    public InputStream download(String url, RpcRequest rpcRequest) {
        HttpHeaders httpHeaders = new HttpHeaders();
        return execInterceptors(httpHeaders, url, rpcRequest, () -> {
            Mono<ClientResponse> clientResponseMono = webClient.post().uri(url + RpcUrl.DOWNLOAD).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).headers(headers -> headers.addAll(httpHeaders)).bodyValue(JSON.toJSONString(rpcRequest)).accept(MediaType.APPLICATION_OCTET_STREAM).exchange();

            return downloadInputStream(clientResponseMono);
        });
    }


    /**
     * 执行所有的拦截器
     *
     * @param endpoint 端点
     * @param request  请求
     * @param supplier function
     * @return T
     */
    private <T> T execInterceptors(HttpHeaders httpHeaders, String endpoint, RpcRequest request, Supplier<T> supplier) {
        RpcAttribute rpcAttribute = new RpcAttribute(endpoint, request.getVersion(), request.getMethod());
        rpcAttribute.setHeaders(httpHeaders);
        rpcAttribute.setRealParams(request.getParams());
        Proceed proceed = supplier::get;
        Object execute = loadMrRequestInterceptor().execute(rpcAttribute, proceed);
        return (T) execute;
    }


    @SneakyThrows
    public InputStream downloadInputStream(Mono<ClientResponse> clientResponse) {
        File file = OsUtil.genTmpFile(cacheDir);
        FileOutputStream outputStream = new FileOutputStream(file, true);
        try (outputStream) {
            clientResponse.flatMapMany(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToFlux(DataBuffer.class);
                } else {
                    return Flux.error(new RpcExecption("Failed to download inputStream. message: " + clientResponse));
                }
            }).flatMap(dataBuffer -> {
                StreamUtil.inputStreamToOutputStream(dataBuffer.asInputStream(), outputStream);
                return Mono.empty();
            }).blockLast(); // 等待下载完成
        }
        return new RpcFileInputStream(file);
    }


}
