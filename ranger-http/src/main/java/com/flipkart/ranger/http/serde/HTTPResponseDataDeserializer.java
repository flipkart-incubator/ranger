package com.flipkart.ranger.http.serde;

import com.flipkart.ranger.core.model.Deserializer;
import com.flipkart.ranger.core.model.ServiceNode;

import java.util.Collection;

/**
 *
 */
@FunctionalInterface
public interface HTTPResponseDataDeserializer<T> extends Deserializer<T> {
    Collection<ServiceNode<T>> deserialize(byte []data);
}
