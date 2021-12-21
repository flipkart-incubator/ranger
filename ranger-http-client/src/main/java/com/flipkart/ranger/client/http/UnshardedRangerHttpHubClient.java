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
package com.flipkart.ranger.client.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.client.AbstractRangerHubClient;
import com.flipkart.ranger.core.finder.nodeselector.RoundRobinServiceNodeSelector;
import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import com.flipkart.ranger.core.finder.shardselector.ListShardSelector;
import com.flipkart.ranger.core.finderhub.ServiceDataSource;
import com.flipkart.ranger.core.finderhub.ServiceFinderFactory;
import com.flipkart.ranger.core.finderhub.ServiceFinderHub;
import com.flipkart.ranger.core.finderhub.StaticDataSource;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.serde.HTTPResponseDataDeserializer;
import com.flipkart.ranger.http.servicefinderhub.HttpServiceDataSource;
import com.flipkart.ranger.http.servicefinderhub.HttpServiceFinderHubBuilder;
import com.flipkart.ranger.http.servicefinderhub.HttpUnshardedServiceFinderFactory;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

@Slf4j
public class UnshardedRangerHttpHubClient<T>
        extends AbstractRangerHubClient<T, ListBasedServiceRegistry<T>, HTTPResponseDataDeserializer<T>> {

    private final Set<Service> services;
    private final HttpClientConfig clientConfig;

    @Builder
    public UnshardedRangerHttpHubClient(
            String namespace,
            ObjectMapper mapper,
            int nodeRefreshIntervalMs,
            Predicate<T> criteria,
            HTTPResponseDataDeserializer<T> deserializer,
            HttpClientConfig clientConfig,
            Set<Service> services
    ) {
        super(namespace, mapper, nodeRefreshIntervalMs, criteria, deserializer);
        this.clientConfig = clientConfig;
        this.services = null != services ? services : Collections.emptySet();
    }

    @Override
    protected ServiceFinderHub<T, ListBasedServiceRegistry<T>> buildHub() {
        return new HttpServiceFinderHubBuilder<T, ListBasedServiceRegistry<T>>()
                .withServiceDataSource(buildServiceDataSource())
                .withServiceFinderFactory(buildFinderFactory())
                .withRefreshFrequencyMs(getNodeRefreshTimeMs())
                .build();
    }

    @Override
    protected ServiceDataSource buildServiceDataSource() {
        return !services.isEmpty() ?
                new StaticDataSource(services) :
                new HttpServiceDataSource<>(clientConfig, getMapper());
    }

    @Override
    protected ServiceFinderFactory<T, ListBasedServiceRegistry<T>> buildFinderFactory() {
        return HttpUnshardedServiceFinderFactory.<T>builder()
                .httpClientConfig(clientConfig)
                .nodeRefreshIntervalMs(getNodeRefreshTimeMs())
                .deserializer(getDeserializer())
                .shardSelector(new ListShardSelector<>())
                .nodeSelector(new RoundRobinServiceNodeSelector<>())
                .mapper(getMapper())
                .build();
    }

}
