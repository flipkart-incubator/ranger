package com.flipkart.ranger.healthservice.monitor;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import org.junit.Assert;
import org.junit.Test;

public class RollingWindowHealthQueueTest {

    @Test
    public void testCheckInRollingWindow1() throws Exception {
        RollingWindowHealthQueue rollingWindowHealthQueue = new RollingWindowHealthQueue(5, 3);
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
    }

    @Test
    public void testCheckInRollingWindowEdge() throws Exception {
        try {
            RollingWindowHealthQueue rollingWindowHealthQueue = new RollingWindowHealthQueue(1, 3);
            Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        } catch (Exception u) {
            Assert.assertTrue(u instanceof UnsupportedOperationException);
        }
    }

    @Test
    public void testCheckInRollingWindowEdge2() throws Exception {
        RollingWindowHealthQueue rollingWindowHealthQueue = new RollingWindowHealthQueue(3, 3);
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
    }

    @Test
    public void testCheckInRollingWindowEdge3() throws Exception {
        RollingWindowHealthQueue rollingWindowHealthQueue = new RollingWindowHealthQueue(5, 1);
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
    }


    @Test
    public void testCheckInRollingWindow2() throws Exception {
        RollingWindowHealthQueue rollingWindowHealthQueue = new RollingWindowHealthQueue(5, 3);
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
    }


    @Test
    public void testCheckInRollingWindow3() throws Exception {
        RollingWindowHealthQueue rollingWindowHealthQueue = new RollingWindowHealthQueue(5, 3);
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
    }

    @Test
    public void testCheckInRollingWindow4() throws Exception {
        RollingWindowHealthQueue rollingWindowHealthQueue = new RollingWindowHealthQueue(5, 3);
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.HEALTHY));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.UNHEALTHY));
    }
}