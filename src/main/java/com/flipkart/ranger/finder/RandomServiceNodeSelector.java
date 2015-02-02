package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.model.ServiceNodeSelector;

import java.util.List;
import java.util.Random;

public class RandomServiceNodeSelector<T> implements ServiceNodeSelector<T> {
    private Random random = new Random(System.currentTimeMillis());

    @Override
    public ServiceNode<T> select(List<ServiceNode<T>> serviceNodes) {
        return serviceNodes.get(random.nextInt(serviceNodes.size()));
    }
}
