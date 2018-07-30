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

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.model.ServiceNode;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class HttpServiceRegistryUpdater<T> extends AbstractServiceRegistryUpdater<T> {
    private static final Logger logger = LoggerFactory.getLogger(HttpServiceRegistryUpdater.class);

    private HttpSourceConfig config;
    private CloseableHttpClient httpclient;
    private URI uri;

    protected HttpServiceRegistryUpdater(HttpSourceConfig config) throws Exception {
        this.config = config;

        final String host = config.getHost();
        final Integer port = config.getPort();
        final String path = config.getPath();
        this.uri = new URIBuilder()
                .setScheme("http")
                .setHost(host)
                .setPort(port)
                .setPath(path)
                .build();
    }

    @Override
    public void start() throws Exception {
        httpclient = HttpClients.createDefault();

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

            HttpGet httpget = new HttpGet(uri);

            try (CloseableHttpResponse response = httpclient.execute(httpget)) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    logger.error("Error in Http get");
                    return null;
                }

                byte[] data = EntityUtils.toByteArray(response.getEntity());
                if (null == data) {
                    logger.warn("No data present");
                    return null;
                }

                List<ServiceNode<T>> serviceNodes = config.getListDeserializer().deserialize(data);
                return serviceNodes.stream()
                        .filter(node -> (node.getHealthcheckStatus() == HealthcheckStatus.healthy && node.getLastUpdatedTimeStamp() > healthcheckZombieCheckThresholdTime))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error getting service data from http: ", e);
        }
        return null;
    }
}
