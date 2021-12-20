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
package com.flipkart.ranger.client.zk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.client.RangerClient;
import com.flipkart.ranger.client.RangerClientConstants;
import com.flipkart.ranger.core.finder.SimpleShardedServiceFinder;
import com.flipkart.ranger.core.finder.shardselector.MatchingShardSelector;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.zookeeper.ServiceFinderBuilders;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataDeserializer;
import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@Getter
public class SimpleRangerZKClient<T> implements RangerClient<T> {

    private final SimpleShardedServiceFinder<T> serviceFinder;
    private final Predicate<T> criteria;
    private final ZkNodeDataDeserializer<T> deserializer;

    @Builder(builderMethodName = "fromConnectionString", builderClassName = "FromConnectionStringBuilder")
    public SimpleRangerZKClient(
            String namespace,
            String serviceName,
            ObjectMapper mapper,
            int nodeRefreshIntervalMs,
            boolean disableWatchers,
            String connectionString,
            Predicate<T> criteria,
            ZkNodeDataDeserializer<T> deserializer
    ){
        this(
                namespace,
                serviceName,
                mapper,
                nodeRefreshIntervalMs,
                disableWatchers,
                CuratorFrameworkFactory.newClient(connectionString, new RetryForever(RangerClientConstants.CONNECTION_RETRY_TIME)),
                criteria,
                deserializer
        );
    }

    @Builder(builderMethodName = "fromCurator", builderClassName = "FromCuratorBuilder")
    public SimpleRangerZKClient(
            String namespace,
            String serviceName,
            ObjectMapper mapper,
            int nodeRefreshIntervalMs,
            boolean disableWatchers,
            CuratorFramework curatorFramework,
            Predicate<T> criteria,
            ZkNodeDataDeserializer<T> deserializer
    ){
        Preconditions.checkNotNull(mapper, "Mapper can't be null");
        Preconditions.checkNotNull(namespace, "namespace can't be null");
        Preconditions.checkNotNull(deserializer, "deserializer can't be null");

        int effectiveRefreshTime = nodeRefreshIntervalMs;
        if (effectiveRefreshTime < RangerClientConstants.MINIMUM_REFRESH_TIME) {
            effectiveRefreshTime = RangerClientConstants.MINIMUM_REFRESH_TIME;
            log.warn("Node info update interval too low: {} ms. Has been upgraded to {} ms ",
                    nodeRefreshIntervalMs,
                    RangerClientConstants.MINIMUM_REFRESH_TIME);
        }

        this.criteria = criteria;
        this.deserializer = deserializer;
        this.serviceFinder = ServiceFinderBuilders.<T>shardedFinderBuilder()
                .withCuratorFramework(curatorFramework)
                .withNamespace(namespace)
                .withServiceName(serviceName)
                .withDeserializer(deserializer)
                .withNodeRefreshIntervalMs(effectiveRefreshTime)
                .withDisableWatchers(disableWatchers)
                .withShardSelector(new MatchingShardSelector<>())
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

    @Override
    public Optional<ServiceNode<T>> getNode() {
        return getNode(criteria);
    }

    @Override
    public List<ServiceNode<T>> getAllNodes() {
        return getAllNodes(criteria);
    }

    @Override
    public Optional<ServiceNode<T>> getNode(Predicate<T> criteria) {
        return this.serviceFinder.get(criteria);
    }

    @Override
    public List<ServiceNode<T>> getAllNodes(Predicate<T> criteria) {
        return this.serviceFinder.getAll(criteria);
    }
}
