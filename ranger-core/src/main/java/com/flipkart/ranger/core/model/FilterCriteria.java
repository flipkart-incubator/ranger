package com.flipkart.ranger.core.model;

@FunctionalInterface
public interface FilterCriteria<T> extends Criteria<T> {

    boolean apply(T nodeData);

}
