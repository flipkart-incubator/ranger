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
package com.flipkart.ranger.server.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.client.Constants;
import com.flipkart.ranger.client.zk.UnshardedZKHubClient;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Deserializer;
import com.flipkart.ranger.server.config.RangerConfiguration;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataDeserializer;
import com.google.common.base.Preconditions;
import io.dropwizard.lifecycle.Managed;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@Getter
public class RangerClientManager<T, C extends Criteria<T>, D extends ZkNodeDataDeserializer<T>> implements Managed {

    private final RangerConfiguration rangerConfiguration;

    private UnshardedZKHubClient<T, C> zkHubClient;
    private CuratorFramework curatorFramework;

    @Inject
    public RangerClientManager(
            RangerConfiguration rangerConfiguration,
            ObjectMapper mapper,
            D deserializer
    ){
        this.rangerConfiguration = rangerConfiguration;
        Preconditions.checkNotNull(rangerConfiguration, "Ranger configuration can't be null");
        Preconditions.checkNotNull(rangerConfiguration.getZookeeper(), "Zookeeper can't be null");
        Preconditions.checkNotNull(rangerConfiguration.getNamespace(), "Namespace can't be null");
        Preconditions.checkNotNull(deserializer, "Deserializer can't be null");

        curatorFramework = CuratorFrameworkFactory.newClient(
                rangerConfiguration.getZookeeper(),
                new RetryForever(Constants.CONNECTION_RETRY_TIME)
        );
        zkHubClient = UnshardedZKHubClient.<T, C>builder()
                .connectionString(rangerConfiguration.getZookeeper())
                .curatorFramework(curatorFramework)
                .disablePushUpdaters(rangerConfiguration.isDisablePushUpdaters())
                .mapper(mapper)
                .services(rangerConfiguration.getServices())
                .refreshTimeMs(rangerConfiguration.getRefreshTimeMs())
                .deserializer(deserializer)
                .build();
    }

    @Override
    public void start() {
        log.info("Starting the ranger client manager");
        curatorFramework.start();
        zkHubClient.start();
        log.info("Started the ranger client manager");
    }

    @Override
    public void stop() {
        log.info("Stopping the ranger client manager");
        zkHubClient.stop();
        curatorFramework.close();
        log.info("Stopped the ranger client manager");
    }
}
