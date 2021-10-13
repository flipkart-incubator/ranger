package com.flipkart.ranger.zookeeper.servicefinderhub;

import com.flipkart.ranger.core.finder.SimpleShardedServiceFinder;
import com.flipkart.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import com.flipkart.ranger.core.finderhub.ServiceFinderFactory;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNodeSelector;
import com.flipkart.ranger.core.model.ShardSelector;
import com.flipkart.ranger.core.model.ShardedCriteria;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataDeserializer;
import com.flipkart.ranger.zookeeper.servicefinder.ZkSimpleShardedServiceFinderBuilder;
import lombok.Builder;
import lombok.val;
import org.apache.curator.framework.CuratorFramework;

/**
 *
 */
public class ZkShardedServiceFinderFactory<T> implements ServiceFinderFactory<T, MapBasedServiceRegistry<T>, ShardedCriteria<T>> {
    private final CuratorFramework curatorFramework;
    private final String connectionString;
    private final int nodeRefreshIntervalMs;
    private final boolean disablePushUpdaters;
    private final ZkNodeDataDeserializer<T> deserializer;
    private final ShardSelector<T, MapBasedServiceRegistry<T>, ShardedCriteria<T>> shardSelector;
    private final ServiceNodeSelector<T> nodeSelector;

    @Builder
    public ZkShardedServiceFinderFactory(
            CuratorFramework curatorFramework,
            String connectionString,
            int nodeRefreshIntervalMs,
            boolean disablePushUpdaters,
            ZkNodeDataDeserializer<T> deserializer,
            ShardSelector<T, MapBasedServiceRegistry<T>, ShardedCriteria<T>> shardSelector,
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
    public SimpleShardedServiceFinder<T> buildFinder(Service service) {
        val finder = new ZkSimpleShardedServiceFinderBuilder<T>()
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
