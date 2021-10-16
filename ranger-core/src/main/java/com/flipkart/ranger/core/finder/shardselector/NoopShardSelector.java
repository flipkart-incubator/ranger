package com.flipkart.ranger.core.finder.shardselector;

import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.ShardSelector;

import java.util.List;

public class NoopShardSelector<T, C extends Criteria<T>> implements ShardSelector<T, C, ListBasedServiceRegistry<T>> {

    @Override
    public List<ServiceNode<T>> nodes(C criteria, ListBasedServiceRegistry<T> serviceRegistry) {
        return serviceRegistry.nodeList();
    }
}
