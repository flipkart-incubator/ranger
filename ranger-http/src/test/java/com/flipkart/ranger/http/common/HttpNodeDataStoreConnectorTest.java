package com.flipkart.ranger.http.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.http.config.HttpClientConfig;
import org.junit.Assert;
import org.junit.Test;

public class HttpNodeDataStoreConnectorTest {
    
    @Test
    public void testHttpNodeDataStoreConnector(){
        final ObjectMapper objectMapper = new ObjectMapper();
        final HttpClientConfig httpClientConfig = HttpClientConfig.builder()
                .host("localhost-1")
                .port(80)
                .build();
        HttpNodeDataStoreConnector httpNodeDataStoreConnector = new HttpNodeDataStoreConnector(httpClientConfig, objectMapper);
        Assert.assertNotNull(httpNodeDataStoreConnector);
        Assert.assertTrue(httpNodeDataStoreConnector.isActive());
    }
}
