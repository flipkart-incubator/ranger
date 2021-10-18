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
package com.flipkart.ranger.server.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.client.RangerClientConstants;
import com.flipkart.ranger.client.zk.UnshardedRangerZKHubClient;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.server.AppConfiguration;
import com.flipkart.ranger.server.model.ShardInfo;
import com.google.common.base.Preconditions;
import io.dropwizard.lifecycle.Managed;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
@Getter
@Slf4j
public class RangerClientManager implements Managed {

    private final AppConfiguration appConfiguration;
    private final CuratorFramework curatorFramework;
    private final UnshardedRangerZKHubClient<ShardInfo, Criteria<ShardInfo>> hubClient;

    @Inject
    public RangerClientManager(AppConfiguration appConfiguration, ObjectMapper mapper){
        Preconditions.checkNotNull(appConfiguration, "Ranger configuration can't be null");
        Preconditions.checkNotNull(appConfiguration.getZookeeper(), "Zookeeper can't be null");
        Preconditions.checkNotNull(appConfiguration.getNamespace(), "Namespace can't be null");
        Preconditions.checkNotNull(mapper, "Mapper can't be null");

        this.appConfiguration = appConfiguration;
        this.curatorFramework = CuratorFrameworkFactory.newClient(
                appConfiguration.getZookeeper(),
                new RetryForever(RangerClientConstants.CONNECTION_RETRY_TIME)
        );
        this.hubClient = UnshardedRangerZKHubClient.<ShardInfo, Criteria<ShardInfo>>builder()
                .connectionString(appConfiguration.getZookeeper())
                .curatorFramework(curatorFramework)
                .disablePushUpdaters(appConfiguration.isDisablePushUpdaters())
                .mapper(mapper)
                .services(appConfiguration.getServices())
                .refreshTimeMs(appConfiguration.getRefreshTimeMs())
                .deserializer(data -> {
                    try {
                        mapper.readValue(data, new TypeReference<ServiceNode<ShardInfo>>() {
                        });
                    } catch (IOException e) {
                        log.warn("Error parsing node data");
                    }
                    return null;
                })
                .build();
    }

    @Override
    public void start() {
        log.info("Starting the ranger client manager");
        curatorFramework.start();
        try {
            curatorFramework.blockUntilConnected();
        }
        catch (InterruptedException e) {
            log.error("Curator block interrupted", e);
        }
        hubClient.start();
        log.info("Started the ranger client manager");
    }

    @Override
    public void stop() {
        log.info("Stopping the ranger client manager");
        hubClient.stop();
        curatorFramework.close();
        log.info("Stopped the ranger client manager");
    }
}
