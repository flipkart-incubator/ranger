package com.flipkart.ranger.server.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flipkart.ranger.client.RangerClientConstants;
import com.flipkart.ranger.core.model.Service;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RangerConfiguration {
    @NotEmpty
    @NotNull
    private String namespace;
    @NotEmpty
    @NotNull
    private String zookeeper;
    private boolean disablePushUpdaters;
    @Min(1000)
    private int nodeRefreshTimeMs = RangerClientConstants.MINIMUM_REFRESH_TIME;
    private List<Service> services;
}
