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

    Optional<List<ServiceNode<T>>> refresh();

    boolean isActive();

    void updateState(ServiceNode<T> serviceNode);
}
