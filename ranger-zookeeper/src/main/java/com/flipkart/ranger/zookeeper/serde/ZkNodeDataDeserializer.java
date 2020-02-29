package com.flipkart.ranger.zookeeper.serde;

import com.flipkart.ranger.core.model.Deserializer;
import com.flipkart.ranger.core.model.ServiceNode;

/**
 *
 */
@FunctionalInterface
public interface ZkNodeDataDeserializer<T> extends Deserializer<T> {
    ServiceNode<T> deserialize(final byte[] data);
}
