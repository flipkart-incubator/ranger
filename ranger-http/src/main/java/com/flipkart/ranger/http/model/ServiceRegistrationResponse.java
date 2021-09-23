package com.flipkart.ranger.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
public class ServiceRegistrationResponse {
    private final boolean success;

    @Builder
    public ServiceRegistrationResponse(
            @JsonProperty("success") boolean success
    ) {
        this.success = success;
    }
}
