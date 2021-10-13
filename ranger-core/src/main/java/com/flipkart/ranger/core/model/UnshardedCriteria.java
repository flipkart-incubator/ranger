package com.flipkart.ranger.core.model;

@FunctionalInterface
public interface UnshardedCriteria<T> extends Criteria<T> {

    boolean apply(T nodeData);

}
