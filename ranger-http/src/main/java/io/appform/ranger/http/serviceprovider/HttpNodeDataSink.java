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
package io.appform.ranger.http.serviceprovider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import io.appform.ranger.core.model.NodeDataSink;
import io.appform.ranger.core.model.Service;
import io.appform.ranger.core.model.ServiceNode;
import io.appform.ranger.core.util.Exceptions;
import io.appform.ranger.http.common.HttpNodeDataStoreConnector;
import io.appform.ranger.http.config.HttpClientConfig;
import io.appform.ranger.http.model.ServiceRegistrationResponse;
import io.appform.ranger.http.serde.HttpRequestDataSerializer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class HttpNodeDataSink<T, S extends HttpRequestDataSerializer<T>> extends HttpNodeDataStoreConnector<T> implements NodeDataSink<T, S> {

    private final Service service;

    public HttpNodeDataSink(Service service, HttpClientConfig config, ObjectMapper mapper) {
        super(config, mapper);
        this.service = service;
    }

    @Override
    public void updateState(S serializer, ServiceNode<T> serviceNode) {
        Preconditions.checkNotNull(config, "client config has not been set for node data");
        Preconditions.checkNotNull(mapper, "mapper has not been set for node data");

        val url = String.format("/ranger/nodes/v1/add/%s/%s", service.getNamespace(), service.getServiceName());
        log.debug("Updating state at the url {}", url);

        val httpUrl = new HttpUrl.Builder()
                .scheme(config.isSecure()
                        ? "https"
                        : "http")
                .host(config.getHost())
                .port(config.getPort() == 0
                        ? defaultPort()
                        : config.getPort())
                .encodedPath(url)
                .build();
        val requestBody = RequestBody.create(serializer.serialize(serviceNode));
        val serviceRegistrationResponse = registerService(httpUrl, requestBody).orElse(null);
        if(null == serviceRegistrationResponse || !serviceRegistrationResponse.valid()){
            log.warn("Http call to {} returned a failure response {}", httpUrl, serviceRegistrationResponse);
            Exceptions.illegalState("Error updating state on the server for node data: " + httpUrl);
        }
    }

    private Optional<ServiceRegistrationResponse<T>> registerService(HttpUrl httpUrl, RequestBody requestBody){
        val request = new Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();
        try (val response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                try (val body = response.body()) {
                    if (null == body) {
                        log.warn("HTTP call to {} returned empty body", httpUrl);
                    }
                    else {
                        return Optional.of(mapper.readValue(body.bytes(),
                                                            new TypeReference<ServiceRegistrationResponse<T>>() {}));
                    }
                }
            }
            else {
                log.warn("HTTP call to {} has returned: {}", httpUrl, response.code());
            }
        }
        catch (IOException e) {
            log.error("Error updating state on the server with httpUrl {} with exception {} ",  httpUrl, e);
        }
        return Optional.empty();
    }
}
