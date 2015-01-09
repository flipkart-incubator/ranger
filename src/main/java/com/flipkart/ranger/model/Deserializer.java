package com.flipkart.ranger.model;

public interface Deserializer<T> {
    public ServiceNode<T> deserialize(final byte[] data);
}
