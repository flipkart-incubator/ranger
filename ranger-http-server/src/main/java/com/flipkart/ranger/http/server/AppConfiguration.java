package com.flipkart.ranger.http.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flipkart.ranger.http.server.config.RangerHttpConfiguration;
import io.dropwizard.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfiguration extends Configuration {
    @NotEmpty
    @NotNull
    private String name;

    @Valid
    @NotNull
    private RangerHttpConfiguration rangerConfiguration;

    private boolean initialRotationStatus;
}
