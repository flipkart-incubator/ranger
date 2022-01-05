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
package com.flipkart.ranger.zk.server.bundle;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.core.type.TypeReference;
import com.flipkart.ranger.client.RangerClientConstants;
import com.flipkart.ranger.client.RangerHubClient;
import com.flipkart.ranger.client.zk.UnshardedRangerZKHubClient;
import com.flipkart.ranger.common.server.ShardInfo;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.signals.Signal;
import com.flipkart.ranger.zk.server.AppConfiguration;
import com.flipkart.ranger.zk.server.healthcheck.RangerHealthCheck;
import com.flipkart.ranger.zk.server.lifecycle.CuratorLifecycle;
import com.google.common.collect.ImmutableList;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

@Slf4j
@Singleton
@NoArgsConstructor
public class ZKServerBundle extends RangerServerBundle<ShardInfo, AppConfiguration> {

    private CuratorFramework curatorFramework;

    @Override
    protected void preBundle(AppConfiguration configuration) {
        curatorFramework = CuratorFrameworkFactory.newClient(
                configuration.getRangerConfiguration().getZookeeper(),
                new RetryForever(RangerClientConstants.CONNECTION_RETRY_TIME)
        );
    }

    @Override
    protected List<RangerHubClient<ShardInfo>> withHubs(AppConfiguration configuration) {
        val rangerConfiguration = configuration.getRangerConfiguration();
        return ImmutableList.of(UnshardedRangerZKHubClient.<ShardInfo>builder()
                .namespace(rangerConfiguration.getNamespace())
                .connectionString(rangerConfiguration.getZookeeper())
                .curatorFramework(curatorFramework)
                .disablePushUpdaters(rangerConfiguration.isDisablePushUpdaters())
                .mapper(getMapper())
                .refreshTimeMs(rangerConfiguration.getNodeRefreshTimeMs())
                .deserializer(data -> {
                    try {
                        return getMapper().readValue(data, new TypeReference<ServiceNode<ShardInfo>>() {
                        });
                    } catch (IOException e) {
                        log.warn("Error parsing node data with value {}", new String(data));
                    }
                    return null;
                })
                .build());
    }

    @Override
    protected boolean withInitialRotationStatus(AppConfiguration configuration) {
        return configuration.isInitialRotationStatus();
    }

    @Override
    protected List<Signal<ShardInfo>> withLifecycleSignals(AppConfiguration configuration) {
        return ImmutableList.of(
                new CuratorLifecycle(curatorFramework)
        );
    }

    @Override
    protected List<HealthCheck> withHealthChecks(AppConfiguration configuration) {
        return ImmutableList.of(new RangerHealthCheck(curatorFramework));
    }
}
