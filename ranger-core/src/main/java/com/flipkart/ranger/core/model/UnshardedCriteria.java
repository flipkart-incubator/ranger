package com.flipkart.ranger.core.model;

import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;

@FunctionalInterface
public interface UnshardedCriteria<T> extends Criteria<T, ListBasedServiceRegistry<T>> {

    boolean apply(T nodeData);

}
