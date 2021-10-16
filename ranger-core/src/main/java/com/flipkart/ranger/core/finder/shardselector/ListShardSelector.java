package com.flipkart.ranger.core.finder.shardselector;

import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.ShardSelector;

import java.util.List;
import java.util.stream.Collectors;

public class ListShardSelector<T> implements ShardSelector<T, ListBasedServiceRegistry<T>> {

    @Override
    public List<ServiceNode<T>> nodes(T criteria, ListBasedServiceRegistry<T> serviceRegistry) {
        if(null == criteria){
            return serviceRegistry.nodeList();
        }
        return serviceRegistry.nodeList().stream().filter(node -> criteria.equals(node.getNodeData())).collect(Collectors.toList());
    }

}
