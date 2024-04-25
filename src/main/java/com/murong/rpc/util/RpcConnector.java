package com.murong.rpc.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.murong.rpc.constant.RpcCodeEnum;
import com.murong.rpc.constant.RpcUrl;
import com.murong.rpc.exception.RpcExecption;
import com.murong.rpc.interact.MrRequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


/**
 * <p>
 * AuthRpc
 * </p>
 *
 * @author yaochuang 2024/04/01 15:54
 */

@RequiredArgsConstructor
public class RpcConnector {

    private final RestTemplate rpcRestTemplate;

    private final RestTemplate streamRestTemplate;

    private final LinkedList<MrRequestInterceptor> interceptors;

    /**
     * 返回单个对象
     *
     * @param url        请求url
     * @param httpMethod 方法体
     * @param tclass     返回类型
     * @param <T>        泛型t
     * @return 放回结果
     */
    public <T> T exchangeRpcType(String url, HttpMethod httpMethod, Class<T> tclass, RpcRequest rpcRequest) {
        HttpHeaders httpHeaders = new HttpHeaders();
        return execInterceptors(httpHeaders, url, rpcRequest, () -> {
            HttpEntity<String> httpEntity = new HttpEntity<>(JSON.toJSONString(rpcRequest), httpHeaders);
            ResponseEntity<String> forEntity = rpcRestTemplate.exchange(url + RpcUrl.RPC, httpMethod, httpEntity, String.class);
            int value = forEntity.getStatusCode().value();
            if (value == RpcCodeEnum.SUCCESS_CODE) {
                return JSONObject.parseObject(forEntity.getBody(), tclass);
            } else if (value == RpcCodeEnum.FAIL_CODE) {
                throw new RpcExecption(value, "rpc异常:" + forEntity.getBody());
            } else {
                throw new RpcExecption(value, "未知异常");
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
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url + RpcUrl.UPLOAD);
            builder.queryParam("param", param);
            URI uri = builder.build().toUri();
            RequestCallback requestCallback = request -> {
                // 继续处理请求头
                request.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
                OutputStream outputStream = request.getBody();
                long length = StreamUtil.inputStreamToOutputStream(inputStream, outputStream);
                request.getHeaders().setContentLength(length);
            };
            ResponseExtractor<T> responseExtractor = response -> {
                int value = response.getStatusCode().value();
                InputStream body = response.getBody();
                String result = IOUtils.toString(body, Charset.defaultCharset());
                if (value == RpcCodeEnum.SUCCESS_CODE) {
                    return JSON.parseObject(result, genericReturnType);
                } else if (value == RpcCodeEnum.FAIL_CODE) {
                    throw new RpcExecption(value, "rpc上传异常:" + result);
                } else {
                    throw new RpcExecption(value, result);
                }
            };
            return streamRestTemplate.execute(uri.toString(), HttpMethod.POST, requestCallback, responseExtractor);
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
            RequestCallback requestCallback = request -> {
                Set<Map.Entry<String, List<String>>> entries = httpHeaders.entrySet();
                for (Map.Entry<String, List<String>> header : entries) {
                    String key = header.getKey();
                    List<String> value = header.getValue();
                    for (String headerValue : value) {
                        request.getHeaders().add(key, headerValue);
                    }
                }
            };
            ResponseExtractor<InputStream> responseExtractor = response -> {
                int value = response.getStatusCode().value();
                InputStream body = response.getBody();
                if (value == RpcCodeEnum.SUCCESS_CODE) {
                    return new RpcFileInputStream(body);
                } else if (value == RpcCodeEnum.FAIL_CODE) {
                    String result = IOUtils.toString(body, Charset.defaultCharset());
                    throw new RpcExecption(value, "rpc下载异常:" + result);
                } else {
                    String result = IOUtils.toString(body, Charset.defaultCharset());
                    throw new RpcExecption(value, result);
                }
            };
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url + RpcUrl.DOWNLOAD);
            builder.queryParam("param", JSON.toJSONString(rpcRequest));
            URI uri = builder.build().toUri();
            return streamRestTemplate.execute(uri.toString(), HttpMethod.POST, requestCallback, responseExtractor);
        });
    }


    /**
     * 执行所有的拦截器
     *
     * @param endpoint 端点
     * @param request  请求
     * @param supplier function
     * @return
     */
    private <T> T execInterceptors(HttpHeaders httpHeaders, String endpoint, RpcRequest request, Supplier<T> supplier) {
        if (!interceptors.isEmpty()) {
            RpcAttribute rpcAttribute = new RpcAttribute();
            rpcAttribute.setEndpoint(endpoint);
            rpcAttribute.setHeaders(httpHeaders);
            rpcAttribute.setMethod(request.getMethod());
            rpcAttribute.setVersion(request.getVersion());
            rpcAttribute.setRealParams(request.getParams());
            return loopProceed(0, interceptors, rpcAttribute, supplier, new ArrayList<T>());
        }
        return supplier.get();
    }


    private <T> T loopProceed(int index, LinkedList<MrRequestInterceptor> allInceptors, RpcAttribute rpcAttribute, Supplier<T> supplier, final List<T> result) {
        if (index >= interceptors.size()) {
            result.add(supplier.get());
        } else {
            MrRequestInterceptor current = allInceptors.get(index);
            current.execute(rpcAttribute, () -> loopProceed(index + 1, allInceptors, rpcAttribute, supplier, result));
        }
        if (result.isEmpty()) {
            return null;
        }
        return result.getFirst();
    }
}
