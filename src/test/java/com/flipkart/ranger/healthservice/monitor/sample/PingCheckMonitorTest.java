package com.flipkart.ranger.healthservice.monitor.sample;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.healthservice.TimeEntity;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class PingCheckMonitorTest {

    @Test
    public void testMonitor() throws Exception {
        final HttpGet httpRequest = new HttpGet("/");
        PingCheckMonitor pingCheckMonitor = new PingCheckMonitor(new TimeEntity(2, TimeUnit.SECONDS), httpRequest, 5000, 5, 3, "google.com", 80);
        Assert.assertEquals(HealthcheckStatus.healthy, pingCheckMonitor.monitor());
        Assert.assertEquals(HealthcheckStatus.healthy, pingCheckMonitor.monitor());
        Assert.assertEquals(HealthcheckStatus.healthy, pingCheckMonitor.monitor());
        Assert.assertEquals(HealthcheckStatus.healthy, pingCheckMonitor.monitor());
        Assert.assertEquals(HealthcheckStatus.healthy, pingCheckMonitor.monitor());
        Assert.assertEquals(HealthcheckStatus.healthy, pingCheckMonitor.monitor());

    }

    @Test
    public void testMonitor2() throws Exception {
        final HttpGet httpRequest = new HttpGet("/help");
        PingCheckMonitor pingCheckMonitor = new PingCheckMonitor(new TimeEntity(2, TimeUnit.SECONDS), httpRequest, 5000, 5, 3, "google.com", 80);
        Assert.assertEquals(HealthcheckStatus.healthy, pingCheckMonitor.monitor());
        Assert.assertEquals(HealthcheckStatus.healthy, pingCheckMonitor.monitor());
        Assert.assertEquals(HealthcheckStatus.unhealthy, pingCheckMonitor.monitor());
        Assert.assertEquals(HealthcheckStatus.unhealthy, pingCheckMonitor.monitor());

    }
}