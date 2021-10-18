/*
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.ranger.zookeeper.healthservice.monitor;

import com.flipkart.ranger.core.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.core.healthservice.monitor.RollingWindowHealthQueue;
import org.junit.Assert;
import org.junit.Test;

public class RollingWindowHealthQueueTest {

    @Test
    public void testCheckInRollingWindow1() throws Exception {
        RollingWindowHealthQueue rollingWindowHealthQueue = new RollingWindowHealthQueue(5, 3);
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
    }

    @Test
    public void testCheckInRollingWindowEdge() throws Exception {
        try {
            RollingWindowHealthQueue rollingWindowHealthQueue = new RollingWindowHealthQueue(1, 3);
            Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        } catch (Exception u) {
            Assert.assertTrue(u instanceof UnsupportedOperationException);
        }
    }

    @Test
    public void testCheckInRollingWindowEdge2() throws Exception {
        RollingWindowHealthQueue rollingWindowHealthQueue = new RollingWindowHealthQueue(3, 3);
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
    }

    @Test
    public void testCheckInRollingWindowEdge3() throws Exception {
        RollingWindowHealthQueue rollingWindowHealthQueue = new RollingWindowHealthQueue(5, 1);
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
    }


    @Test
    public void testCheckInRollingWindow2() throws Exception {
        RollingWindowHealthQueue rollingWindowHealthQueue = new RollingWindowHealthQueue(5, 3);
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
    }


    @Test
    public void testCheckInRollingWindow3() throws Exception {
        RollingWindowHealthQueue rollingWindowHealthQueue = new RollingWindowHealthQueue(5, 3);
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
    }

    @Test
    public void testCheckInRollingWindow4() throws Exception {
        RollingWindowHealthQueue rollingWindowHealthQueue = new RollingWindowHealthQueue(5, 3);
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
        Assert.assertFalse(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.healthy));
        Assert.assertTrue(rollingWindowHealthQueue.checkInRollingWindow(HealthcheckStatus.unhealthy));
    }
}