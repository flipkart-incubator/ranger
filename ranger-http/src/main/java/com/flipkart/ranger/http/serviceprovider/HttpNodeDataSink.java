package com.flipkart.ranger.http.serviceprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.model.NodeDataSink;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.util.Exceptions;
import com.flipkart.ranger.http.common.HttpNodeDataStoreConnector;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.serde.HttpRequestDataSerializer;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

@Slf4j
public class HttpNodeDataSink<T, S extends HttpRequestDataSerializer<T>> extends HttpNodeDataStoreConnector<T> implements NodeDataSink<T, S> {

    public HttpNodeDataSink(Service service, HttpClientConfig config, ObjectMapper mapper) {
        super(service, config, mapper);
    }

    @Override
    public void updateState(S serializer, ServiceNode<T> serviceNode) {
        Preconditions.checkNotNull(serializer, "Serializer has not been set for node data");
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
        RequestBody requestBody = RequestBody.create(serializer.serialize(serviceNode));
        val request = new Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();
        try (val response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("HTTP call to {} returned: {}", httpUrl.toString(), response.code());
            }
        }
        catch (IOException e) {
            Exceptions.illegalState("Error updating the server with service data: " + httpUrl, e);
        }
    }
}
