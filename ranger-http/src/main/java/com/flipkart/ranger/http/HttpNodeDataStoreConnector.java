package com.flipkart.ranger.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.model.NodeDataStoreConnector;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.http.config.HttpClientConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
@Slf4j
public class HttpNodeDataStoreConnector<T> implements NodeDataStoreConnector<T> {

    protected final Service service;
    protected final HttpClientConfig config;
    protected final OkHttpClient httpClient;
    protected final ObjectMapper mapper;
    protected final AtomicBoolean firstCall = new AtomicBoolean(false);

    public HttpNodeDataStoreConnector(
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

    protected int defaultPort() {
        return config.isSecure()
               ? 443
               : 80;
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
