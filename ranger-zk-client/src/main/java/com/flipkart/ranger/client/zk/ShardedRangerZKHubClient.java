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
package com.flipkart.ranger.client.zk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.finder.nodeselector.RoundRobinServiceNodeSelector;
import com.flipkart.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import com.flipkart.ranger.core.finder.shardselector.MatchingShardSelector;
import com.flipkart.ranger.core.finderhub.ServiceFinderFactory;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataDeserializer;
import com.flipkart.ranger.zookeeper.servicefinderhub.ZkShardedServiceFinderFactory;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.util.Set;
import java.util.function.Predicate;

@Slf4j
public class ShardedRangerZKHubClient<T>
        extends AbstractRangerZKHubClient<T, MapBasedServiceRegistry<T>, ZkNodeDataDeserializer<T>> {

    @Builder
    public ShardedRangerZKHubClient(
            String namespace,
            ObjectMapper mapper,
            int refreshTimeMs,
            boolean disablePushUpdaters,
            String connectionString,
            CuratorFramework curatorFramework,
            Predicate<T> criteria,
            ZkNodeDataDeserializer<T> deserializer,
            Set<Service> services,
            boolean alwaysUseInitialCriteria
    ) {
        super(
                namespace,
                mapper,
                refreshTimeMs,
                disablePushUpdaters,
                connectionString,
                curatorFramework,
                criteria,
                deserializer,
                services,
                alwaysUseInitialCriteria
        );
    }

    @Override
    protected ServiceFinderFactory<T, MapBasedServiceRegistry<T>> buildFinderFactory() {
        return ZkShardedServiceFinderFactory.<T>builder()
                .curatorFramework(getCuratorFramework())
                .connectionString(getConnectionString())
                .nodeRefreshIntervalMs(getNodeRefreshTimeMs())
                .disablePushUpdaters(isDisablePushUpdaters())
                .deserializer(getDeserializer())
                .shardSelector(new MatchingShardSelector<>())
                .nodeSelector(new RoundRobinServiceNodeSelector<>())
                .build();
    }
}

