package com.flipkart.ranger.http.config;

import lombok.*;

/**
 *
 */
@Data
@AllArgsConstructor
@Builder
@ToString
@NoArgsConstructor
public class HttpClientConfig {
    private String host;
    private int port;
    private boolean secure;
    private long connectionTimeoutMs;
    private long operationTimeoutMs;
    private long refreshIntervalMillis;
}
