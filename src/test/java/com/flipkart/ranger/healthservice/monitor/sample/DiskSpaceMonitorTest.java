package com.flipkart.ranger.healthservice.monitor.sample;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.healthservice.TimeEntity;
import com.flipkart.ranger.healthservice.monitor.IsolatedHealthMonitor;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class DiskSpaceMonitorTest {

    IsolatedHealthMonitor diskSpaceMonitor = new DiskSpaceMonitor("/", 1000, new TimeEntity(2, TimeUnit.SECONDS));

    @Test
    public void testGetCount() throws Exception {
        Assert.assertEquals(HealthcheckStatus.healthy, diskSpaceMonitor.monitor());
        Assert.assertEquals(HealthcheckStatus.healthy, diskSpaceMonitor.monitor());
        Assert.assertEquals(HealthcheckStatus.healthy, diskSpaceMonitor.monitor());
        Assert.assertEquals(HealthcheckStatus.healthy, diskSpaceMonitor.monitor());
        Assert.assertEquals(HealthcheckStatus.healthy, diskSpaceMonitor.monitor());
        Assert.assertEquals(HealthcheckStatus.healthy, diskSpaceMonitor.monitor());
    }
}