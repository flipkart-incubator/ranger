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
package com.flipkart.ranger.zookeeper.servicefinderhub;

import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinder;
import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import com.flipkart.ranger.core.finderhub.ServiceFinderFactory;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNodeSelector;
import com.flipkart.ranger.core.model.ShardSelector;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataDeserializer;
import com.flipkart.ranger.zookeeper.servicefinder.ZkSimpleUnshardedServiceFinderBuilder;
import lombok.Builder;
import lombok.val;
import org.apache.curator.framework.CuratorFramework;


public class ZKUnshardedServiceFinderFactory<T> implements ServiceFinderFactory<T, ListBasedServiceRegistry<T>>{

    private final CuratorFramework curatorFramework;
    private final String connectionString;
    private final int nodeRefreshIntervalMs;
    private final boolean disablePushUpdaters;
    private final ZkNodeDataDeserializer<T> deserializer;
    private final ShardSelector<T, ListBasedServiceRegistry<T>> shardSelector;
    private final ServiceNodeSelector<T> nodeSelector;

    @Builder
    public ZKUnshardedServiceFinderFactory(
            CuratorFramework curatorFramework,
            String connectionString,
            int nodeRefreshIntervalMs,
            boolean disablePushUpdaters,
            ZkNodeDataDeserializer<T> deserializer,
            ShardSelector<T, ListBasedServiceRegistry<T>> shardSelector,
            ServiceNodeSelector<T> nodeSelector) {
        this.curatorFramework = curatorFramework;
        this.connectionString = connectionString;
        this.nodeRefreshIntervalMs = nodeRefreshIntervalMs;
        this.disablePushUpdaters = disablePushUpdaters;
        this.deserializer = deserializer;
        this.shardSelector = shardSelector;
        this.nodeSelector = nodeSelector;
    }

    @Override
    public SimpleUnshardedServiceFinder<T> buildFinder(Service service) {
        val finder = new ZkSimpleUnshardedServiceFinderBuilder<T>()
                .withDeserializer(deserializer)
                .withNamespace(service.getNamespace())
                .withServiceName(service.getServiceName())
                .withNodeRefreshIntervalMs(nodeRefreshIntervalMs)
                .withDisableWatchers(disablePushUpdaters)
                .withShardSelector(shardSelector)
                .withNodeSelector(nodeSelector)
                .withConnectionString(connectionString)
                .withCuratorFramework(curatorFramework)
                .build();
        finder.start();
        return finder;
    }
}
