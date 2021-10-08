package com.flipkart.ranger.core.model;

import com.flipkart.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;

public abstract class ShardedCriteria<T> implements Criteria<T, MapBasedServiceRegistry<T>> {

    public abstract T getShard();

}
