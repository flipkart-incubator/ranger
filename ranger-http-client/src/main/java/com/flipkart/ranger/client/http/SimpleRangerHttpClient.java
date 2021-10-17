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
package com.flipkart.ranger.client.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.client.RangerClient;
import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinder;
import com.flipkart.ranger.core.finder.shardselector.ListShardSelector;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.http.HttpServiceFinderBuilders;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.serde.HTTPResponseDataDeserializer;
import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class SimpleRangerHttpClient<T, C extends Criteria<T>, D extends HTTPResponseDataDeserializer<T>> implements RangerClient<T, C, D> {

    private final C criteria;
    private final D deserializer;
    private final SimpleUnshardedServiceFinder<T, C> serviceFinder;

    @Builder
    public SimpleRangerHttpClient(
            String namespace,
            String serviceName,
            ObjectMapper mapper,
            int refreshTimeMs,
            HttpClientConfig clientConfig,
            C criteria,
            D deserializer
    ) {
        Preconditions.checkNotNull(mapper, "Mapper can't be null");
        Preconditions.checkNotNull(namespace, "namespace can't be null");
        Preconditions.checkNotNull(deserializer, "deserializer can't be null");

        this.criteria = criteria;
        this.deserializer = deserializer;

        this.serviceFinder = HttpServiceFinderBuilders.<T, C>httpUnshardedServiceFinderBuilider()
                .withClientConfig(clientConfig)
                .withServiceName(serviceName)
                .withNamespace(namespace)
                .withObjectMapper(mapper)
                .withNodeRefreshIntervalMs(refreshTimeMs)
                .withDeserializer(deserializer)
                .withShardSelector(new ListShardSelector<>())
                .build();
    }

    @Override
    public void start() {
        log.info("Starting the service finder");
        this.serviceFinder.start();
    }

    @Override
    public void stop() {
        log.info("Stopping the service finder");
        this.serviceFinder.stop();
    }

    @Override
    public Optional<ServiceNode<T>> getNode() {
        return getNode(criteria);
    }

    @Override
    public Optional<List<ServiceNode<T>>> getAllNodes() {
        return getAllNodes(criteria);
    }

    @Override
    public Optional<ServiceNode<T>> getNode(C criteria) {
        return Optional.ofNullable(this.serviceFinder.get(criteria));
    }

    @Override
    public Optional<List<ServiceNode<T>>> getAllNodes(C criteria) {
        return Optional.ofNullable(this.serviceFinder.getAll(criteria));
    }
}

