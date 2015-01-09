package com.flipkart.ranger.finder.unsharded;

import com.flipkart.ranger.finder.BaseServiceFinderBuilder;
import com.flipkart.ranger.finder.Service;
import com.flipkart.ranger.model.*;

public class UnshardedFinderBuilder extends BaseServiceFinderBuilder<UnshardedClusterInfo, UnshardedClusterServiceRegistry, UnshardedClusterFinder> {
    @Override
    protected UnshardedClusterFinder buildFinder(Service service, Deserializer<UnshardedClusterInfo> deserializer, ShardSelector<UnshardedClusterInfo, UnshardedClusterServiceRegistry> shardSelector, ServiceNodeSelector<UnshardedClusterInfo> nodeSelector) {
        UnshardedClusterServiceRegistry unshardedClusterServiceRegistry = new UnshardedClusterServiceRegistry(service, deserializer);
        return new UnshardedClusterFinder(unshardedClusterServiceRegistry, new NoOpShardSelector(), nodeSelector);
    }
}
