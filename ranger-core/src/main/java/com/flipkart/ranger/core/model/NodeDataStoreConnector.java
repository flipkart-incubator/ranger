package com.flipkart.ranger.core.model;

/**
 *
 */
public interface NodeDataStoreConnector<T> {
    void start();

    void ensureConnected();

    void stop();

    boolean isActive();
}
