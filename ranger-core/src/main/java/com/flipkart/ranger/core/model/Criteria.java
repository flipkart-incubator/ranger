package com.flipkart.ranger.core.model;

@FunctionalInterface
public interface Criteria<T> {
    boolean apply(T nodeData);
}
