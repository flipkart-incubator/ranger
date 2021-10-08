package com.flipkart.ranger.core.model;

import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;

public abstract class UnshardedCriteria<T> implements Criteria<T, ListBasedServiceRegistry<T>> {

    public abstract boolean apply(T nodeData);

}
