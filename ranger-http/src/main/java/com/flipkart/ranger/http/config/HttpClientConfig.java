package com.flipkart.ranger.http.config;

import lombok.Builder;
import lombok.Data;

/**
 *
 */
@Data
@Builder
public class HttpClientConfig {
    private String host;
    private int port;
    private boolean secure;
    private long connectionTimeoutMs;
    private long operationTimeoutMs;
    private String nodesRequestPath;

}
