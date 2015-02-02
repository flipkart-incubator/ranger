package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.model.ServiceNodeSelector;
import com.flipkart.ranger.model.ServiceRegistry;
import com.flipkart.ranger.model.ShardSelector;
import com.google.common.collect.Lists;

import java.util.List;

public class ServiceFinder<T, ServiceRegistryType extends ServiceRegistry<T>> {
    private final ServiceRegistryType serviceRegistry;
    private final ShardSelector<T, ServiceRegistryType> shardSelector;
    private final ServiceNodeSelector<T> nodeSelector;

    public ServiceFinder(ServiceRegistryType serviceRegistry, ShardSelector<T, ServiceRegistryType> shardSelector, ServiceNodeSelector<T> nodeSelector) {
        this.serviceRegistry = serviceRegistry;
        this.shardSelector = shardSelector;
        this.nodeSelector = nodeSelector;
    }

    public ServiceNode<T> get(T criteria) {
        List<ServiceNode<T>> nodes = shardSelector.nodes(criteria, serviceRegistry);
        if(null == nodes || nodes.isEmpty()) {
            return null;
        }
        return nodeSelector.select(nodes);
    }

    public void start() throws Exception {
        serviceRegistry.start();
    }

    public void stop() throws Exception {
        serviceRegistry.stop();
    }
}
