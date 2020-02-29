package com.flipkart.ranger.core.model;

/**
 *
 */
public interface NodeDataSink<T, S extends Serializer<T>> extends NodeDataStoreConnector<T> {
    void updateState(S serializer, ServiceNode<T> serviceNode);
}
