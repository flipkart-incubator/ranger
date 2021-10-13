package com.flipkart.ranger.core.model;

@FunctionalInterface
public interface ShardedCriteria<T> extends Criteria<T> {

    T getShard();

}
