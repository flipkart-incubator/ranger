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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.finder.nodeselector.RoundRobinServiceNodeSelector;
import com.flipkart.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import com.flipkart.ranger.core.finder.shardselector.MatchingShardSelector;
import com.flipkart.ranger.core.finderhub.ServiceDataSource;
import com.flipkart.ranger.core.finderhub.ServiceFinderFactory;
import com.flipkart.ranger.core.finderhub.ServiceFinderHub;
import com.flipkart.ranger.core.finderhub.StaticDataSource;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.model.ServiceNodesResponse;
import com.flipkart.ranger.http.servicefinderhub.HttpServiceDataSource;
import com.flipkart.ranger.http.servicefinderhub.HttpServiceFinderHubBuilder;
import com.flipkart.ranger.http.servicefinderhub.HttpShardedServiceFinderFactory;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ShardedHttpHubClient<T, C extends Criteria<T>> extends AbstractHttpHubClient<T, C, MapBasedServiceRegistry<T>> {

    private List<Service> services;
    private final HttpClientConfig clientConfig;

    @Builder
    public ShardedHttpHubClient(
            String namespace,
            String environment,
            ObjectMapper mapper,
            int refreshTimeMs,
            C criteria,
            HttpClientConfig clientConfig,
            List<Service> services
    ) {
        super(namespace, environment, mapper, refreshTimeMs, criteria);
        this.clientConfig = clientConfig;
        this.services = services;
    }

    @Override
    public ServiceFinderHub<T, C, MapBasedServiceRegistry<T>> buildHub() {
        return new HttpServiceFinderHubBuilder<T, C, MapBasedServiceRegistry<T>>()
                .withServiceDataSource(getServiceDataSource())
                .withServiceFinderFactory(getFinderFactory())
                .withRefreshFrequencyMs(getRefreshTimeMs())
                .build();
    }

    /*
           In case of http hub, if client could provide the services for which hub needs to be refreshed, use them instead.
    */
    @Override
    public ServiceDataSource getServiceDataSource() {
        return null != services && !services.isEmpty() ?
                new StaticDataSource(services) :
                new HttpServiceDataSource<>(clientConfig, getMapper());
    }

    @Override
    public ServiceFinderFactory<T, C, MapBasedServiceRegistry<T>> getFinderFactory() {
        return HttpShardedServiceFinderFactory.<T, C>builder()
                .httpClientConfig(clientConfig)
                .nodeRefreshIntervalMs(getRefreshTimeMs())
                .deserializer(data -> {
                    try{
                        return getMapper().readValue(data, new TypeReference<ServiceNodesResponse<T>>() {});
                    }catch (IOException e){
                        log.warn("Could not parse node data");
                    }
                    return null;
                })
                .shardSelector(new MatchingShardSelector<>())
                .nodeSelector(new RoundRobinServiceNodeSelector<>())
                .build();
    }
}
