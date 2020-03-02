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
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
@Slf4j
public class HttpNodeDataSource<T, D extends HTTPResponseDataDeserializer<T>> extends HttpNodeDataStoreConnector<T> implements NodeDataSource<T, D> {

    private final AtomicBoolean firstCall = new AtomicBoolean(false);

    public HttpNodeDataSource(
            Service service,
            final HttpClientConfig config,
            ObjectMapper mapper) {
        super(service, config, mapper);
    }


    @Override
    public Optional<List<ServiceNode<T>>> refresh(D deserializer) {
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
            if (response.isSuccessful()) {
                final ResponseBody body = response.body();
                if (null == body) {
                    log.warn("HTTP call to {} returned empty body", httpUrl.toString());
                }
                else {
                    final byte[] bytes;
                    try {
                        bytes = body.bytes();
                    }
                    finally {
                        if(null != body) {
                            body.close();
                        }
                    }
                    return Optional.of(FinderUtils.filterValidNodes(
                            service,
                            deserializer.deserialize(bytes),
                            healthcheckZombieCheckThresholdTime(service)));
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

    @Override
    public boolean isActive() {
//        return httpClient.connectionPool().connectionCount() > 0;
        return true;
    }
}
