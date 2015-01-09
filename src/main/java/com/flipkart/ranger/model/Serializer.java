package com.flipkart.ranger.model;

public interface Serializer<T> {
    public byte[] serialize(final ServiceNode<T> data);
}
