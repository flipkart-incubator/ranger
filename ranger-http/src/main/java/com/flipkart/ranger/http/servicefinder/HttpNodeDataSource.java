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
package com.flipkart.ranger.http.servicefinder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.model.NodeDataSource;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.util.Exceptions;
import com.flipkart.ranger.core.util.FinderUtils;
import com.flipkart.ranger.http.common.HttpNodeDataStoreConnector;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.serde.HTTPResponseDataDeserializer;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 */
@Slf4j
public class HttpNodeDataSource<T, D extends HTTPResponseDataDeserializer<T>> extends HttpNodeDataStoreConnector<T> implements NodeDataSource<T, D> {

    private final Service service;

    public HttpNodeDataSource(
            Service service,
            final HttpClientConfig config,
            ObjectMapper mapper) {
        super(config, mapper);
        this.service = service;
    }

    @Override
    public Optional<List<ServiceNode<T>>> refresh(D deserializer) {
        Preconditions.checkNotNull(config, "client config has not been set for node data");
        Preconditions.checkNotNull(mapper, "mapper has not been set for node data");
        val httpUrl = new HttpUrl.Builder()
                .scheme(config.isSecure()
                        ? "https"
                        : "http")
                .host(config.getHost())
                .port(config.getPort() == 0
                        ? defaultPort()
                        : config.getPort())
                .encodedPath(String.format("/ranger/nodes/v1/%s/%s", service.getNamespace(), service.getServiceName()))
                .build();
        val request = new Request.Builder()
                .url(httpUrl)
                .get()
                .build();

        List<ServiceNode<T>> serviceNodes = Collections.emptyList();
        try (val response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                try (final ResponseBody body = response.body()) {
                    if (null == body) {
                        log.warn("HTTP call to {} returned empty body", httpUrl);
                    } else {
                        final byte[] bytes = body.bytes();
                        val serviceNodesResponse = deserializer.deserialize(bytes);
                        if(serviceNodesResponse.isSuccess()){
                            serviceNodes = FinderUtils.filterValidNodes(
                                    service,
                                    serviceNodesResponse.getData(),
                                    healthcheckZombieCheckThresholdTime(service));
                        }else{
                            log.warn("Http call to {} returned a failure response with error {}", httpUrl, serviceNodesResponse.getError());
                        }
                    }
                }
            } else {
                log.warn("HTTP call to {} returned: {}", httpUrl, response.code());
            }
        } catch (IOException e) {
            log.error("Error getting service data from the http endPoint: ", e);
        }
        return Optional.of(serviceNodes);
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
