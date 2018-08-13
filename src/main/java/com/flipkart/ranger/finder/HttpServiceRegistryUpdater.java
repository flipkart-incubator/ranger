/**
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.ranger.finder;

import com.flipkart.ranger.finder.HttpVerb.HttpVerbFactory;
import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.model.ServiceNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class HttpServiceRegistryUpdater<T> extends ServiceRegistryUpdater<T> {
    private static final Logger logger = LoggerFactory.getLogger(HttpServiceRegistryUpdater.class);

    private HttpSourceConfig<T> httpSourceConfig;
    private CloseableHttpClient httpclient;
    private URI uri;
    private HttpVerbFactory httpVerbFactory;
    private String scheme;

    protected HttpServiceRegistryUpdater(HttpSourceConfig<T> httpSourceConfig) throws Exception {
        this.httpSourceConfig = httpSourceConfig;
        final String host = this.httpSourceConfig.getHost();
        final int port = this.httpSourceConfig.getPort();
        final String path = this.httpSourceConfig.getPath();

        if(httpSourceConfig.isSecure()) {
            this.scheme = "https";
        } else {
            this.scheme = "http";
        }
        this.uri = new URIBuilder()
                .setScheme(scheme)
                .setHost(host)
                .setPort(port)
                .setPath(path)
                .build();
        this.httpVerbFactory = new HttpVerbFactory();
    }

    @Override
    public void start() throws Exception {
        if(this.scheme == "https" && this.httpSourceConfig.isSuppressHostCheck()){
            httpclient = HttpClients.custom().setSSLHostnameVerifier((s, sslSession) -> true).build();
        } else {
            httpclient = HttpClients.createDefault();
        }
        httpclient = HttpClients.custom().setSSLHostnameVerifier((s, sslSession) -> true).build();

        serviceRegistry.nodes(getHealthyServiceNodes());
        logger.info("Started http updater");
    }

    @Override
    public void stop() throws Exception {
        httpclient.close();
        logger.debug("Stopped http updater");
    }

    @Override
    protected List<ServiceNode<T>> getHealthyServiceNodes() {
        try {
            final long healthcheckZombieCheckThresholdTime = System.currentTimeMillis() - 60000; //1 Minute

            HttpRequestBase  httpRequestBase = httpVerbFactory.getHttpVerb(httpSourceConfig.getHttpVerb(), uri);

            try (CloseableHttpResponse response = httpclient.execute(httpRequestBase)) {
                int status = response.getStatusLine().getStatusCode();
                if (status < 200 && status >= 300) {
                    logger.error("Error in Http get, Status Code: " + response.getStatusLine().getStatusCode() + " received Response: " + response);
                    return null;
                }

                byte[] data = EntityUtils.toByteArray(response.getEntity());
                if (null == data) {
                    logger.warn("No data present");
                    return null;
                }

                List<ServiceNode<T>> serviceNodes = httpSourceConfig.getHttpResponseDecoder().deserialize(data);
                return serviceNodes.stream()
                        .filter(node -> (node.getHealthcheckStatus() == HealthcheckStatus.healthy &&
                                node.getLastUpdatedTimeStamp() > healthcheckZombieCheckThresholdTime))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error getting service data from http: ", e);
        }
        return null;
    }
}
