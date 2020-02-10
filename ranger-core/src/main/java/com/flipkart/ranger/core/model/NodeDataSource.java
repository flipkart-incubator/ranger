package com.flipkart.ranger.core.model;

import java.util.List;
import java.util.Optional;

/**
 *
 */
public interface NodeDataSource<T> {
    void start();

    void ensureConnected();

    void stop();

    Optional<List<ServiceNode<T>>> refresh(Deserializer<T> deserializer);

    boolean isActive();

    void updateState(Serializer<T> serializer, ServiceNode<T> serviceNode);
}
