package com.flipkart.ranger.http.serde;

import com.flipkart.ranger.core.model.Serializer;
import com.flipkart.ranger.core.model.ServiceNode;

@FunctionalInterface
public interface HttpRequestDataSerializer<T> extends Serializer<T> {

    byte[] serialize(final ServiceNode<T> node);

}
