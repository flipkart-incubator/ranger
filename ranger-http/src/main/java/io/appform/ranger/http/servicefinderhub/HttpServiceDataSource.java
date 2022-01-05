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
package io.appform.ranger.http.servicefinderhub;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.appform.ranger.core.finderhub.ServiceDataSource;
import io.appform.ranger.core.model.Service;
import io.appform.ranger.http.common.HttpNodeDataStoreConnector;
import io.appform.ranger.http.config.HttpClientConfig;
import io.appform.ranger.http.model.ServiceDataSourceResponse;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.HttpUrl;
import okhttp3.Request;

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
                .encodedPath("/ranger/services/v1")
                .build();
        val request = new Request.Builder()
                .url(httpUrl)
                .get()
                .build();
        
        try (val response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                try (val body = response.body()) {
                    if (null == body) {
                        log.warn("HTTP call to {} returned empty body", httpUrl);
                    }
                    else {
                        val bytes = body.bytes();
                        val serviceDataSourceResponse = mapper.readValue(bytes, ServiceDataSourceResponse.class);
                        if(serviceDataSourceResponse.valid()){
                            return serviceDataSourceResponse.getData();
                        }else{
                            log.warn("Http call to {} returned a failure response with data {}", httpUrl, serviceDataSourceResponse);
                        }
                    }
                }
            }
            else {
                log.warn("HTTP call to {} returned code: {}", httpUrl, response.code());
            }
        }
        catch (IOException e) {
            log.info("Error parsing the response from server for : {} with exception {}", httpUrl, e);
        }

        log.error("No data returned from server: " + httpUrl);
        return Collections.emptySet();
    }
}
