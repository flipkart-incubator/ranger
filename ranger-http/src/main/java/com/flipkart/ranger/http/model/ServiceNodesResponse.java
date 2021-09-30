package com.flipkart.ranger.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.ranger.core.model.ServiceNode;
import lombok.*;

import java.util.List;

/**
 *
 */
@Data
public class ServiceNodesResponse<T> {
    private final boolean success;
    final List<ServiceNode<T>> nodes;

    @Builder
    public ServiceNodesResponse(
            @JsonProperty("success") boolean success,
            @JsonProperty("nodes") @Singular List<ServiceNode<T>> nodes) {
        this.success = success;
        this.nodes = nodes;
    }
}
