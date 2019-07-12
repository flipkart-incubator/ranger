/**
 * Copyright 2016 Flipkart Internet Pvt. Ltd.
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

package com.flipkart.ranger.healthservice;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.healthservice.monitor.IsolatedHealthMonitor;
import com.flipkart.ranger.healthservice.monitor.Monitor;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class ServiceHealthAggregatorTest {

    @Test
    public void testStaleRun() throws Exception {
        ServiceHealthAggregator serviceHealthAggregator = new ServiceHealthAggregator();
        TestMonitor testMonitor = new TestMonitor("TestHealthMonitor", TimeEntity.everySecond(), 1000, false);
        serviceHealthAggregator.addIsolatedMonitor(testMonitor);
        serviceHealthAggregator.addInlineMonitor(new Monitor<HealthcheckStatus>() {
            @Override
            public HealthcheckStatus monitor() {
                return HealthcheckStatus.healthy;
            }

            @Override
            public boolean isDisabled() {
                return false;
            }
        });

        serviceHealthAggregator.start();
        Thread.sleep(1000);
        testMonitor.run();
        testMonitor.setThreadSleep(2000);

        Thread.sleep(4000);

        /* in the TestMonitor, thread was sleeping for 2 seconds, */
        /* so its state is supposed to be stale (>1 second) and service has to be unhealthy */
        Assert.assertEquals(HealthcheckStatus.unhealthy, serviceHealthAggregator.getServiceHealth());


        testMonitor.setThreadSleep(10);
        Thread.sleep(4000);

        /* in the TestMonitor, thread is sleeping only for 10 milliseconds, */
        /* so its state is supposed to be NOT stale (>1 second) and service has to be healthy */
        Assert.assertEquals(HealthcheckStatus.healthy, serviceHealthAggregator.getServiceHealth());
        serviceHealthAggregator.stop();
    }

    @Test
    public void testDelayedRun() throws Exception {
        ServiceHealthAggregator serviceHealthAggregator = new ServiceHealthAggregator();
        TestMonitor testMonitor = new TestMonitor("TestHealthMonitor",
                                                  new TimeEntity(5, 1, TimeUnit.SECONDS),
                                                  1000,
                                                  true);
        serviceHealthAggregator.addIsolatedMonitor(testMonitor);
        serviceHealthAggregator.addInlineMonitor(new Monitor<HealthcheckStatus>() {
            @Override
            public HealthcheckStatus monitor() {
                return HealthcheckStatus.healthy;
            }

            @Override
            public boolean isDisabled() {
                return false;
            }
        });

        serviceHealthAggregator.start();

        // This should return healthy as it the monitor will not be evaluated
        Assert.assertEquals(HealthcheckStatus.healthy, serviceHealthAggregator.getServiceHealth());


        Thread.sleep(6000);

        // This should return unhealthy as the monitor will return unhealthy now
        Assert.assertEquals(HealthcheckStatus.unhealthy, serviceHealthAggregator.getServiceHealth());
        serviceHealthAggregator.stop();
    }

    private class TestMonitor extends IsolatedHealthMonitor {
        private final boolean forceUnhealthy;
        int threadSleep = 2000;

        public TestMonitor(String name, TimeEntity timeEntity, boolean forceUnhealthy) {
            super(name, timeEntity);
            this.forceUnhealthy = forceUnhealthy;
        }

        public TestMonitor(String name, TimeEntity timeEntity, long stalenessAllowedInMillis, boolean forceUnhealthy) {
            super(name, timeEntity, stalenessAllowedInMillis);
            this.forceUnhealthy = forceUnhealthy;
        }

        public void setThreadSleep(int threadSleep) {
            this.threadSleep = threadSleep;
        }

        @Override
        public synchronized HealthcheckStatus monitor() {
            try {
                Thread.sleep(threadSleep);
            }
            catch (InterruptedException e) {
            }
            return forceUnhealthy
                   ? HealthcheckStatus.unhealthy
                   : HealthcheckStatus.healthy;
        }
    }
}