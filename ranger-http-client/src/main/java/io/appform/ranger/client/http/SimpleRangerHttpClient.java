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
package io.appform.ranger.client.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.appform.ranger.client.AbstractRangerClient;
import io.appform.ranger.core.finder.SimpleUnshardedServiceFinder;
import io.appform.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import io.appform.ranger.core.finder.shardselector.ListShardSelector;
import io.appform.ranger.http.HttpServiceFinderBuilders;
import io.appform.ranger.http.config.HttpClientConfig;
import io.appform.ranger.http.serde.HTTPResponseDataDeserializer;
import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Predicate;

@Slf4j
public class SimpleRangerHttpClient<T> extends AbstractRangerClient<T, ListBasedServiceRegistry<T>> {

    @Getter
    private final SimpleUnshardedServiceFinder<T> serviceFinder;

    @Builder
    public SimpleRangerHttpClient(
            String namespace,
            String serviceName,
            ObjectMapper mapper,
            int nodeRefreshIntervalMs,
            HttpClientConfig clientConfig,
            Predicate<T> initialCriteria,
            HTTPResponseDataDeserializer<T> deserializer,
            boolean alwaysUseInitialCriteria
    ) {

        super(initialCriteria, alwaysUseInitialCriteria);

        Preconditions.checkNotNull(mapper, "Mapper can't be null");
        Preconditions.checkNotNull(namespace, "namespace can't be null");
        Preconditions.checkNotNull(deserializer, "deserializer can't be null");

        this.serviceFinder = HttpServiceFinderBuilders.<T>httpUnshardedServiceFinderBuilider()
                .withClientConfig(clientConfig)
                .withServiceName(serviceName)
                .withNamespace(namespace)
                .withObjectMapper(mapper)
                .withNodeRefreshIntervalMs(nodeRefreshIntervalMs)
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

}

