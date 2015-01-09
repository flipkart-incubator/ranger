package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.model.ServiceNodeSelector;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinServiceNodeSelector<T> implements ServiceNodeSelector<T> {
    private AtomicInteger index = new AtomicInteger(0);

    @Override
    public ServiceNode<T> select(List<ServiceNode<T>> serviceNodes) {
        if(serviceNodes == null || serviceNodes.isEmpty()) {
            return null;
        }
        index.set(index.incrementAndGet() % serviceNodes.size());
        return serviceNodes.get(index.get());
    }
}
