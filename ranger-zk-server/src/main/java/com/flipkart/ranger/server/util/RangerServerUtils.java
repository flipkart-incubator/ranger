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
package com.flipkart.ranger.server.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.client.RangerClientConstants;
import com.flipkart.ranger.client.zk.UnshardedRangerZKHubClient;
import com.flipkart.ranger.common.server.ShardInfo;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.server.config.RangerConfiguration;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;

import java.io.IOException;

@Slf4j
public class RangerServerUtils {

    private RangerServerUtils(){}

    public static void verifyPreconditions(RangerConfiguration rangerConfiguration){
        Preconditions.checkNotNull(rangerConfiguration,
                "ranger configuration can't be null");
        Preconditions.checkNotNull(rangerConfiguration.getNamespace(),
                "Namespace can't be null");
        Preconditions.checkNotNull(rangerConfiguration.getZookeeper(),
                "Zookeeper can't be null");
    }

    public static CuratorFramework buildCurator(RangerConfiguration rangerConfiguration){
        return CuratorFrameworkFactory.newClient(
                rangerConfiguration.getZookeeper(),
                new RetryForever(RangerClientConstants.CONNECTION_RETRY_TIME)
        );
    }

    public static UnshardedRangerZKHubClient<ShardInfo, Criteria<ShardInfo>> buildRangerHub(
            CuratorFramework curatorFramework,
            RangerConfiguration rangerConfiguration,
            ObjectMapper mapper
    ){
        return UnshardedRangerZKHubClient.<ShardInfo, Criteria<ShardInfo>>builder()
                .namespace(rangerConfiguration.getNamespace())
                .connectionString(rangerConfiguration.getZookeeper())
                .curatorFramework(curatorFramework)
                .disablePushUpdaters(rangerConfiguration.isDisablePushUpdaters())
                .mapper(mapper)
                .refreshTimeMs(rangerConfiguration.getNodeRefreshTimeMs())
                .deserializer(data -> {
                    try {
                        mapper.readValue(data, new TypeReference<ServiceNode<ShardInfo>>() {
                        });
                    } catch (IOException e) {
                        log.warn("Error parsing node data with value {}", new String(data));
                    }
                    return null;
                })
                .build();
    }
}
