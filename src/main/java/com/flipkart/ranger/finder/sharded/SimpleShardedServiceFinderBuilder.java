package com.flipkart.ranger.finder.sharded;

import com.flipkart.ranger.finder.BaseServiceFinderBuilder;
import com.flipkart.ranger.finder.Service;
import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.ServiceNodeSelector;
import com.flipkart.ranger.model.ShardSelector;

public class SimpleShardedServiceFinderBuilder<T> extends BaseServiceFinderBuilder<T, MapBasedServiceRegistry<T>, SimpleShardedServiceFinder<T>> {
    @Override
    protected SimpleShardedServiceFinder<T> buildFinder(Service service,
                                                        Deserializer<T> deserializer,
                                                        ShardSelector<T, MapBasedServiceRegistry<T>> shardSelector,
                                                        ServiceNodeSelector<T> nodeSelector,
                                                        int healthcheckRefreshTimeMillis) {
        if(null == shardSelector) {
            shardSelector = new MatchingShardSelector<T>();
        }
        MapBasedServiceRegistry<T> serviceRegistry = new MapBasedServiceRegistry<T>(service, deserializer, healthcheckRefreshTimeMillis);
        return new SimpleShardedServiceFinder<T>(serviceRegistry, shardSelector, nodeSelector);
    }
}
