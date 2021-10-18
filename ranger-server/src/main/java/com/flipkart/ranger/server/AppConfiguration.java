package com.flipkart.ranger.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flipkart.ranger.client.RangerClientConstants;
import com.flipkart.ranger.core.model.Service;
import io.dropwizard.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfiguration extends Configuration {

    @NotEmpty
    @NotNull
    private String name;

    @NotEmpty
    @NotNull
    private String zookeeper;

    @NotEmpty
    @NotNull
    private String namespace;

    @Min(1000)
    private int refreshTimeMs = RangerClientConstants.MINIMUM_REFRESH_TIME;

    private boolean disablePushUpdaters = true;

    // Specify these if you don't want to use the data source!
    private List<Service> services;

    private boolean initialRotationStatus;
}
