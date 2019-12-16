package com.flipkart.ranger.datasource;

import com.flipkart.ranger.model.ServiceNode;

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
}
