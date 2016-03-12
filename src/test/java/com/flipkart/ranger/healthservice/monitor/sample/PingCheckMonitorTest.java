/**
 * Copyright 2016 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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