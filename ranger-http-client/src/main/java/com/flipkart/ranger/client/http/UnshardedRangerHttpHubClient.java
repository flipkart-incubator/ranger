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
import com.flipkart.ranger.client.AbstractRangerHubClient;
import com.flipkart.ranger.core.finder.nodeselector.RoundRobinServiceNodeSelector;
import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import com.flipkart.ranger.core.finder.shardselector.ListShardSelector;
import com.flipkart.ranger.core.finderhub.ServiceDataSource;
import com.flipkart.ranger.core.finderhub.ServiceFinderFactory;
import com.flipkart.ranger.core.finderhub.ServiceFinderHub;
import com.flipkart.ranger.core.finderhub.StaticDataSource;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.serde.HTTPResponseDataDeserializer;
import com.flipkart.ranger.http.servicefinderhub.HttpServiceDataSource;
import com.flipkart.ranger.http.servicefinderhub.HttpServiceFinderHubBuilder;
import com.flipkart.ranger.http.servicefinderhub.HttpUnshardedServiceFinderFactory;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class UnshardedRangerHttpHubClient<T, C extends Criteria<T>> extends AbstractRangerHubClient<T, C, ListBasedServiceRegistry<T>, HTTPResponseDataDeserializer<T>> {

    private List<Service> services;
    private final HttpClientConfig clientConfig;

    @Builder
    public UnshardedRangerHttpHubClient(
            String namespace,
            ObjectMapper mapper,
            int nodeRefreshIntervalMs,
            C criteria,
            HTTPResponseDataDeserializer<T> deserializer,
            HttpClientConfig clientConfig,
            List<Service> services
    ) {
        super(namespace, mapper, nodeRefreshIntervalMs, criteria, deserializer);
        this.clientConfig = clientConfig;
        this.services = services;
    }

    @Override
    protected ServiceFinderHub<T, C, ListBasedServiceRegistry<T>> buildHub() {
        return new HttpServiceFinderHubBuilder<T, C, ListBasedServiceRegistry<T>>()
                .withServiceDataSource(buildServiceDataSource())
                .withServiceFinderFactory(buildFinderFactory())
                .withRefreshFrequencyMs(getRefreshTimeMs())
                .build();
    }

    @Override
    protected ServiceDataSource buildServiceDataSource() {
        return null != services && !services.isEmpty() ?
                new StaticDataSource(services) :
                new HttpServiceDataSource<>(clientConfig, getMapper());
    }

    @Override
    protected ServiceFinderFactory<T, C, ListBasedServiceRegistry<T>> buildFinderFactory() {
        return HttpUnshardedServiceFinderFactory.<T, C>builder()
                .httpClientConfig(clientConfig)
                .nodeRefreshIntervalMs(getRefreshTimeMs())
                .deserializer(getDeserializer())
                .shardSelector(new ListShardSelector<>())
                .nodeSelector(new RoundRobinServiceNodeSelector<>())
                .build();
    }

}
