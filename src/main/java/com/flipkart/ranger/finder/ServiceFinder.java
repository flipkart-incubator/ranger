package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.model.ServiceNodeSelector;
import com.flipkart.ranger.model.ServiceRegistry;
import com.flipkart.ranger.model.ShardSelector;

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
        return nodeSelector.select(shardSelector.nodes(criteria, serviceRegistry));
    }

    public void start() throws Exception {
        serviceRegistry.start();
    }
}
