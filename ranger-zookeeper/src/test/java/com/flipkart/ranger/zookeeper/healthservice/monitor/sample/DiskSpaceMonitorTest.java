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

package com.flipkart.ranger.zookeeper.healthservice.monitor.sample;

import com.flipkart.ranger.core.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.core.healthservice.TimeEntity;
import com.flipkart.ranger.core.healthservice.monitor.IsolatedHealthMonitor;
import com.flipkart.ranger.core.healthservice.monitor.sample.DiskSpaceMonitor;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class DiskSpaceMonitorTest {

    final IsolatedHealthMonitor diskSpaceMonitor = new DiskSpaceMonitor("/", 1000, new TimeEntity(2, TimeUnit.SECONDS));

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