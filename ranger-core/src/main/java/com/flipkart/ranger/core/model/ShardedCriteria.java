package com.flipkart.ranger.core.model;

import com.flipkart.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;

@FunctionalInterface
public interface ShardedCriteria<T> extends Criteria<T, MapBasedServiceRegistry<T>> {

    T getShard();

}
