package com.flipkart.ranger.http.serde;

import com.flipkart.ranger.core.model.Deserializer;
import com.flipkart.ranger.http.model.ServiceNodesResponse;

/**
 *
 */
@FunctionalInterface
public interface HTTPResponseDataDeserializer<T> extends Deserializer<T> {
    ServiceNodesResponse<T> deserialize(byte []data);
}
