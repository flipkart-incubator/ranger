package com.flipkart.ranger.finder.sharded;

import com.flipkart.ranger.finder.sharded.MapBasedServiceRegistry;
import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.model.ShardSelector;

import java.util.List;

public class MatchingShardSelector<T> implements ShardSelector<T, MapBasedServiceRegistry<T>> {

    @Override
    public List<ServiceNode<T>> nodes(T criteria, MapBasedServiceRegistry<T> serviceRegistry) {
        return serviceRegistry.nodes().get(criteria);
    }
}
