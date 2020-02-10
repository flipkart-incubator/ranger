package com.flipkart.ranger.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.model.*;
import com.flipkart.ranger.core.util.Exceptions;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.model.ServiceNodesResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Slf4j
public class HttpNodeDataSource<T> implements NodeDataSource<T> {

    private final Service service;
    private final HttpClientConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper mapper;

    public HttpNodeDataSource(
            Service service,
            final HttpClientConfig config,
            ObjectMapper mapper) {
        this.service = service;
        this.httpClient = new OkHttpClient.Builder()
                .callTimeout(config.getOperationTimeoutMs() == 0
                             ? 3000
                             : config.getOperationTimeoutMs(), TimeUnit.MILLISECONDS)
                .connectTimeout(config.getConnectionTimeoutMs() == 0
                                ? 3000
                                : config.getConnectionTimeoutMs(), TimeUnit.MILLISECONDS)
                .followRedirects(true)
                .connectionPool(new ConnectionPool(1, 30, TimeUnit.SECONDS))
                .build();
        this.config = config;
        this.mapper = mapper;
    }


    @Override
    public void start() {
    }

    @Override
    public void ensureConnected() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Optional<List<ServiceNode<T>>> refresh(Deserializer<T> deserializer) {
        final HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(config.isSecure()
                        ? "https"
                        : "http")
                .host(config.getHost())
                .port(config.getPort() == 0
                      ? defaultPort()
                      : config.getPort())
                .encodedPath(String.format("/ranger/nodes/v1/%s/%s", service.getNamespace(), service.getServiceName()))
                .build();

        try {
            final Response response = httpClient.newCall(new Request.Builder()
                                                                .url(httpUrl)
                                                                .get()
                                                                .build())
                    .execute();
            if(response.isSuccessful()) {
                final ResponseBody body = response.body();
                if(null == body) {
                    log.warn("HTTP call to {} returned empty body", httpUrl.toString());
                }
                else {
                    val serviceResponse = translateResponse(body.bytes());
                    if (serviceResponse.isSuccess()) {
                        return Optional.of(serviceResponse.getNodes());
                    }
                    else {
                        log.warn("HTTP call to {} is not successful", httpUrl.toString());
                    }
                }
            }
            else {
                log.warn("HTTP call to {} returned: {}", httpUrl.toString(), response.code());
            }
        }
        catch (IOException e) {
            Exceptions.illegalState(e);
        }
        throw new IllegalStateException("No data received from server");
    }

    private int defaultPort() {
        return config.isSecure()
           ? 443
           : 80;
    }

    @Override
    public boolean isActive() {
        return httpClient.connectionPool().connectionCount() > 0;
    }

    @Override
    public void updateState(Serializer<T> serializer, ServiceNode<T> serviceNode) {
        throw new UnsupportedOperationException("State update is not supported on HTTP yet.");
    }

    protected ServiceNodesResponse<T> translateResponse(final byte data[]) throws IOException {
        return mapper.readValue(data, new TypeReference<ServiceNodesResponse<T>>() {});
    }
}
