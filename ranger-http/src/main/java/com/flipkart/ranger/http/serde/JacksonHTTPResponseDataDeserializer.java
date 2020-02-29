package com.flipkart.ranger.http.serde;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.http.model.ServiceNodesResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 *
 */
public class JacksonHTTPResponseDataDeserializer<T> implements HTTPResponseDataDeserializer<T> {
    private final ObjectMapper mapper;

    public JacksonHTTPResponseDataDeserializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Collection<ServiceNode<T>> deserialize(byte[] data) {
        try {
            final ServiceNodesResponse<T> response
                    = mapper.readValue(data, new TypeReference<ServiceNodesResponse<T>>() {});
            return response.isSuccess() ? response.getNodes() : Collections.emptyList();
        }
        catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
