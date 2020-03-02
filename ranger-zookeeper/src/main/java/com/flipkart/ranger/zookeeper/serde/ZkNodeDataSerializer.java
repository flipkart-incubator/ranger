package com.flipkart.ranger.zookeeper.serde;

import com.flipkart.ranger.core.model.Serializer;
import com.flipkart.ranger.core.model.ServiceNode;

/**
 *
 */
@FunctionalInterface
public interface ZkNodeDataSerializer<T> extends Serializer<T> {
    byte[] serialize(final ServiceNode<T> node);
}
