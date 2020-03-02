package com.flipkart.ranger.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 *
 */
@Data
public class ServiceNodesRequest {
    private final String serviceName;

    @Builder
    public ServiceNodesRequest(@JsonProperty("serviceName") String serviceName) {
        this.serviceName = serviceName;
    }
}
