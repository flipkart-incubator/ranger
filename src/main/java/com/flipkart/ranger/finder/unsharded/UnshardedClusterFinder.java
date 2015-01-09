package com.flipkart.ranger.finder.unsharded;

import com.flipkart.ranger.finder.ServiceFinder;
import com.flipkart.ranger.model.ServiceNodeSelector;
import com.flipkart.ranger.model.ShardSelector;

public class UnshardedClusterFinder extends ServiceFinder<UnshardedClusterInfo, UnshardedClusterServiceRegistry> {
    public UnshardedClusterFinder(UnshardedClusterServiceRegistry serviceRegistry,
                                  ShardSelector<UnshardedClusterInfo, UnshardedClusterServiceRegistry> shardSelector,
                                  ServiceNodeSelector<UnshardedClusterInfo> nodeSelector) {
        super(serviceRegistry, shardSelector, nodeSelector);
    }
}
