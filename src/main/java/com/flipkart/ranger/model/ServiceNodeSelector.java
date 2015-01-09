package com.flipkart.ranger.model;

import java.util.List;

public interface ServiceNodeSelector<T> {
    public ServiceNode<T> select(List<ServiceNode<T>> serviceNodes);
}
