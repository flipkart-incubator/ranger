package com.flipkart.ranger.model;

import java.util.List;

public interface ShardSelector<T, ServiceRegistryType extends ServiceRegistry<T>> {
    public List<ServiceNode<T>> nodes(T criteria, ServiceRegistryType serviceRegistry);
}
