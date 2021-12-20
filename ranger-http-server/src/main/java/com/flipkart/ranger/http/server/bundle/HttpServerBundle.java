/*
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
package com.flipkart.ranger.http.server.bundle;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.core.type.TypeReference;
import com.flipkart.ranger.client.RangerHubClient;
import com.flipkart.ranger.client.http.UnshardedRangerHttpHubClient;
import com.flipkart.ranger.common.server.ShardInfo;
import com.flipkart.ranger.http.model.ServiceNodesResponse;
import com.flipkart.ranger.http.server.AppConfiguration;
import com.flipkart.ranger.zk.server.bundle.RangerServerBundle;
import com.flipkart.ranger.zk.server.bundle.model.LifecycleSignal;
import com.google.common.base.Preconditions;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Singleton
@NoArgsConstructor
public class HttpServerBundle extends RangerServerBundle<ShardInfo, AppConfiguration> {

    @Override
    protected void verifyPreconditions(AppConfiguration configuration) {
        val rangerConfiguration = configuration.getRangerConfiguration();
        Preconditions.checkNotNull(rangerConfiguration,
                "ranger configuration can't be null");
        Preconditions.checkNotNull(rangerConfiguration.getNamespace(),
                "Namespace can't be null");
        Preconditions.checkArgument(null != rangerConfiguration.getHttpClientConfigs() && !rangerConfiguration.getHttpClientConfigs().isEmpty(),
                "Http client config can't be null");
    }

    @Override
    protected void preBundle(AppConfiguration configuration) {
                /*
                    Noop
                 */
    }

    @Override
    protected List<RangerHubClient<ShardInfo>> withHubs(AppConfiguration configuration) {
        val rangerConfiguration = configuration.getRangerConfiguration();
        return rangerConfiguration.getHttpClientConfigs().stream().map(clientConfig -> UnshardedRangerHttpHubClient.<ShardInfo>builder()
                .namespace(rangerConfiguration.getNamespace())
                .mapper(getMapper())
                .clientConfig(clientConfig)
                .nodeRefreshIntervalMs(rangerConfiguration.getNodeRefreshTimeMs())
                .deserializer(data -> {
                    try {
                        getMapper().readValue(data, new TypeReference<ServiceNodesResponse<ShardInfo>>() {
                        });
                    } catch (IOException e) {
                        log.warn("Error parsing node data with value {}", new String(data));
                    }
                    return null;
                })
                .build()).collect(Collectors.toList());
    }

    @Override
    protected boolean withInitialRotationStatus(AppConfiguration configuration) {
        return configuration.isInitialRotationStatus();
    }

    @Override
    protected List<LifecycleSignal> withLifecycleSignals(AppConfiguration configuration) {
        return Collections.emptyList();
    }

    @Override
    protected List<HealthCheck> withHealthChecks(AppConfiguration configuration) {
        return Collections.emptyList();
    }
}
