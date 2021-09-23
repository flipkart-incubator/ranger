package com.flipkart.ranger.http.serviceprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.model.NodeDataSink;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.util.Exceptions;
import com.flipkart.ranger.core.util.FinderUtils;
import com.flipkart.ranger.http.common.HttpNodeDataStoreConnector;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.model.ServiceRegistrationResponse;
import com.flipkart.ranger.http.serde.HttpRequestDataSerializer;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class HttpNodeDataSink<T, S extends HttpRequestDataSerializer<T>> extends HttpNodeDataStoreConnector<T> implements NodeDataSink<T, S> {

    public HttpNodeDataSink(Service service, HttpClientConfig config, ObjectMapper mapper) {
        super(service, config, mapper);
    }

    @Override
    public void updateState(S serializer, ServiceNode<T> serviceNode) {
        val httpUrl = new HttpUrl.Builder()
                .scheme(config.isSecure()
                        ? "https"
                        : "http")
                .host(config.getHost())
                .port(config.getPort() == 0
                        ? defaultPort()
                        : config.getPort())
                .encodedPath(String.format("/ranger/nodes/v1/add/%s/%s", service.getNamespace(), service.getServiceName()))
                .build();
        val requestBody = RequestBody.create(serializer.serialize(serviceNode));
        val serviceRegistrationResponse = registerService(httpUrl, requestBody);
        if(!serviceRegistrationResponse.isPresent() || !serviceRegistrationResponse.get().isSuccess()){
            Exceptions.illegalState("Error updating state on the server for nodedata: " + httpUrl);
        }
    }

    private Optional<ServiceRegistrationResponse> registerService(HttpUrl httpUrl, RequestBody requestBody){
        val request = new Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();
        try (val response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                try (final ResponseBody body = response.body()) {
                    if (null == body) {
                        log.warn("HTTP call to {} returned empty body", httpUrl);
                    }
                    else {
                        final byte[] bytes = body.bytes();
                        return Optional.of(mapper.readValue(bytes, ServiceRegistrationResponse.class));
                    }
                }
            }
            else {
                log.warn("HTTP call to {} has returned: {}", httpUrl, response.code());
            }
        }
        catch (IOException e) {
            Exceptions.illegalState("Error updating state on the server: " + httpUrl, e);
        }
        return Optional.empty();
    }
}
