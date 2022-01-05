/*
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.appform.ranger.http.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.appform.ranger.core.model.NodeDataStoreConnector;
import io.appform.ranger.http.config.HttpClientConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 *
 */
@Slf4j
public class HttpNodeDataStoreConnector<T> implements NodeDataStoreConnector<T> {

    protected final HttpClientConfig config;
    protected final ObjectMapper mapper;
    protected final OkHttpClient httpClient;

    public HttpNodeDataStoreConnector(
            final HttpClientConfig config,
            final ObjectMapper mapper) {
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
        //Nothing to do here
    }

    @Override
    public void ensureConnected() {
        //Nothing to do here
    }

    @Override
    public void stop() {
        //Nothing to do here
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
