package com.flipkart.ranger.http.config;

import com.flipkart.ranger.http.ResourceHelper;
import org.junit.Assert;
import org.junit.Test;

public class HttpClientConfigTest {

    @Test
    public void testHttpClientConfig(){
        HttpClientConfig resource = ResourceHelper.getResource("fixtures/httpClientConfig.json", HttpClientConfig.class);
        Assert.assertEquals("localhost-1", resource.getHost());
        Assert.assertEquals(80, resource.getPort());
        Assert.assertEquals(10, resource.getConnectionTimeoutMs());
        Assert.assertEquals(10, resource.getOperationTimeoutMs());
    }
}
