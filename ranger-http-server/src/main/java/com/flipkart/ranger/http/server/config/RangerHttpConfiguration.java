package com.flipkart.ranger.http.server.config;

import com.flipkart.ranger.http.config.HttpClientConfig;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RangerHttpConfiguration {

    @NotEmpty
    @NotNull
    private String namespace;
    @NotEmpty
    @NotNull
    private List<HttpClientConfig> httpClientConfigs;
    private int nodeRefreshTimeMs;

}
