package com.flipkart.ranger.finder.sharded;

import com.flipkart.ranger.finder.ServiceFinder;
import com.flipkart.ranger.model.ServiceNodeSelector;
import com.flipkart.ranger.model.ShardSelector;

public class SimpleShardedServiceFinder<T> extends ServiceFinder<T, MapBasedServiceRegistry<T>> {
    public SimpleShardedServiceFinder(MapBasedServiceRegistry<T> serviceRegistry,
                                      ShardSelector<T, MapBasedServiceRegistry<T>> shardSelector,
                                      ServiceNodeSelector<T> nodeSelector) {
        super(serviceRegistry, shardSelector, nodeSelector);
    }
}
