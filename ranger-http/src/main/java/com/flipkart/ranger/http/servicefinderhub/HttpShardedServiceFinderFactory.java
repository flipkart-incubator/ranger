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
package com.flipkart.ranger.http.servicefinderhub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.finder.ServiceFinder;
import com.flipkart.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import com.flipkart.ranger.core.finderhub.ServiceFinderFactory;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNodeSelector;
import com.flipkart.ranger.core.model.ShardSelector;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.serde.HTTPResponseDataDeserializer;
import com.flipkart.ranger.http.servicefinder.HttpShardedServiceFinderBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.val;

@Getter
public class HttpShardedServiceFinderFactory <T, C extends Criteria<T>> implements ServiceFinderFactory<T, C, MapBasedServiceRegistry<T>> {

    private final HttpClientConfig clientConfig;
    private final ObjectMapper mapper;
    private final HTTPResponseDataDeserializer<T> deserializer;
    private final ShardSelector<T, C, MapBasedServiceRegistry<T>> shardSelector;
    private final ServiceNodeSelector<T> nodeSelector;
    private final int nodeRefreshIntervalMs;

    @Builder
    public HttpShardedServiceFinderFactory(
            HttpClientConfig httpClientConfig,
            ObjectMapper mapper,
            HTTPResponseDataDeserializer<T> deserializer,
            ShardSelector<T, C, MapBasedServiceRegistry<T>> shardSelector,
            ServiceNodeSelector<T> nodeSelector,
            int nodeRefreshIntervalMs)
    {
        this.clientConfig = httpClientConfig;
        this.mapper = mapper;
        this.deserializer = deserializer;
        this.shardSelector = shardSelector;
        this.nodeSelector = nodeSelector;
        this.nodeRefreshIntervalMs = nodeRefreshIntervalMs;
    }

    @Override
    public ServiceFinder<T, C, MapBasedServiceRegistry<T>> buildFinder(Service service) {
        val serviceFinder = new HttpShardedServiceFinderBuilder<T, C>()
                .withClientConfig(clientConfig)
                .withObjectMapper(mapper)
                .withDeserializer(deserializer)
                .withNamespace(service.getNamespace())
                .withServiceName(service.getServiceName())
                .withNodeRefreshIntervalMs(nodeRefreshIntervalMs)
                .withShardSelector(shardSelector)
                .withNodeSelector(nodeSelector)
                .build();
        serviceFinder.start();
        return serviceFinder;
    }
}
