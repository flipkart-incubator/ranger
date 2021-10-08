package com.flipkart.ranger.core.finder.shardselector;

import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.ShardSelector;
import com.flipkart.ranger.core.model.UnshardedCriteria;

import java.util.List;
import java.util.stream.Collectors;

public class ListBasedShardSelector<T> implements ShardSelector<T, ListBasedServiceRegistry<T>, UnshardedCriteria<T>> {

    @Override
    public List<ServiceNode<T>> nodes(UnshardedCriteria<T> criteria, ListBasedServiceRegistry<T> serviceRegistry) {
        if(null == criteria){
            return serviceRegistry.nodeList();
        }
        return serviceRegistry.nodeList().stream().filter(node -> criteria.apply(node.getNodeData())).collect(Collectors.toList());
    }
}
