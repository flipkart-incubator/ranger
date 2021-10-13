package com.flipkart.ranger.http.servicefinderhub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.finderhub.ServiceDataSource;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.util.Exceptions;
import com.flipkart.ranger.http.common.HttpNodeDataStoreConnector;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.model.ServiceDataSourceResponse;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

@Slf4j
public class HttpServiceDataSource<T> extends HttpNodeDataStoreConnector<T> implements ServiceDataSource {

    public HttpServiceDataSource(HttpClientConfig config, ObjectMapper mapper) {
        super(config, mapper);
    }

    @Override
    public Collection<Service> services() {
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
                .encodedPath("/ranger/services")
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
                        val serviceDataSourceResponse = mapper.readValue(bytes, ServiceDataSourceResponse.class);
                        if(serviceDataSourceResponse.isSuccess()){
                            return serviceDataSourceResponse.getServices();
                        }
                    }
                }
            }
            else {
                log.warn("HTTP call to {} returned: {}", httpUrl, response.code());
            }
        }
        catch (IOException e) {
            Exceptions.illegalState("Error fetching data from server: " + httpUrl, e);
        }

        log.error("No data returned from server: " + httpUrl);
        return Collections.emptyList();
    }
}
