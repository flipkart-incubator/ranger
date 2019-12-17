package com.flipkart.ranger.dataprovider;

import com.flipkart.ranger.model.ServiceNode;

/**
 *
 */
public interface NodeDataProvider<T> {
    void start();

    void ensureConnected();

    void stop();

    boolean isActive();

    void updateState(ServiceNode<T> serviceNode);
}
