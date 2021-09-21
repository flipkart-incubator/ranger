package com.flipkart.ranger.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.model.NodeDataSource;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.util.Exceptions;
import com.flipkart.ranger.core.util.FinderUtils;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.serde.HTTPResponseDataDeserializer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 *
 */
@Slf4j
public class HttpNodeDataSource<T, D extends HTTPResponseDataDeserializer<T>> extends HttpNodeDataStoreConnector<T> implements NodeDataSource<T, D> {
    public HttpNodeDataSource(
            Service service,
            final HttpClientConfig config,
            ObjectMapper mapper) {
        super(service, config, mapper);
    }


    @Override
    public Optional<List<ServiceNode<T>>> refresh(D deserializer) {
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
        try (val response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                try (final ResponseBody body = response.body()) {
                    if (null == body) {
                        log.warn("HTTP call to {} returned empty body", httpUrl);
                    }
                    else {
                        final byte[] bytes = body.bytes();
                        return Optional.of(FinderUtils.filterValidNodes(
                                service,
                                deserializer.deserialize(bytes),
                                healthcheckZombieCheckThresholdTime(service)));
                    }
                }
            }
            else {
                log.warn("HTTP call to {} returned: {}", httpUrl.toString(), response.code());
            }
        }
        catch (IOException e) {
            Exceptions.illegalState("Error fetching data from server: " + httpUrl, e);
        }
        log.error("No data returned from server: " + httpUrl);
        return Optional.empty();
    }

    @Override
    public boolean isActive() {
//        return httpClient.connectionPool().connectionCount() > 0;
        return true;
    }
}
