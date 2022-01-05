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
package io.appform.ranger.client.zk;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.appform.ranger.core.finder.nodeselector.RoundRobinServiceNodeSelector;
import io.appform.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import io.appform.ranger.core.finder.shardselector.ListShardSelector;
import io.appform.ranger.core.finderhub.ServiceFinderFactory;
import io.appform.ranger.core.model.Service;
import io.appform.ranger.zookeeper.serde.ZkNodeDataDeserializer;
import io.appform.ranger.zookeeper.servicefinderhub.ZKUnshardedServiceFinderFactory;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.util.Set;
import java.util.function.Predicate;

@Slf4j
public class UnshardedRangerZKHubClient<T>
        extends AbstractRangerZKHubClient<T, ListBasedServiceRegistry<T>, ZkNodeDataDeserializer<T>> {

    @Builder
    public UnshardedRangerZKHubClient(
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
    protected ServiceFinderFactory<T, ListBasedServiceRegistry<T>> buildFinderFactory() {
        return ZKUnshardedServiceFinderFactory.<T>builder()
            .curatorFramework(getCuratorFramework())
            .connectionString(getConnectionString())
            .nodeRefreshIntervalMs(getNodeRefreshTimeMs())
            .disablePushUpdaters(isDisablePushUpdaters())
            .deserializer(getDeserializer())
            .shardSelector(new ListShardSelector<>())
            .nodeSelector(new RoundRobinServiceNodeSelector<>())
            .build();
    }

}
