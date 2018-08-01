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

package com.flipkart.ranger.healthservice;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.healthservice.monitor.IsolatedHealthMonitor;
import com.flipkart.ranger.healthservice.monitor.Monitor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class ServiceHealthAggregatorTest {


    ServiceHealthAggregator serviceHealthAggregator = new ServiceHealthAggregator();
    TestMonitor testMonitor;
    @Before
    public void setUp() throws Exception {
        testMonitor = new TestMonitor("TestHealthMonitor", TimeEntity.everySecond(), 1000);
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
    }

    @After
    public void tearDown() throws Exception {
        serviceHealthAggregator.stop();
    }

    @Test
    public void testStaleRun() throws Exception {
        testMonitor.run();
        testMonitor.setThreadSleep(3000);

        Thread.sleep(4000);

        /* in the TestMonitor, thread was sleeping for 3 seconds, */
        /* so its state is supposed to be stale (>1 second) and service has to be unhealthy */
        Assert.assertEquals(HealthcheckStatus.unhealthy, serviceHealthAggregator.getServiceHealth());


        testMonitor.setThreadSleep(10);
        Thread.sleep(2000);

        /* in the TestMonitor, thread is sleeping only for 10 milliseconds, */
        /* so its state is supposed to be NOT stale (>1 second) and service has to be healthy */
        Assert.assertEquals(HealthcheckStatus.healthy, serviceHealthAggregator.getServiceHealth());
    }

    private class TestMonitor extends IsolatedHealthMonitor {
        AtomicInteger threadSleep = new AtomicInteger(2000);

        public TestMonitor(String name, TimeEntity timeEntity) {
            super(name, timeEntity);
        }

        public TestMonitor(String name, TimeEntity timeEntity, long stalenessAllowedInMillis) {
            super(name, timeEntity, stalenessAllowedInMillis);
        }

        public void setThreadSleep(int threadSleep) {
            this.threadSleep.set(threadSleep);
        }

        @Override
        public synchronized HealthcheckStatus monitor() {
            try {
                Thread.sleep(threadSleep.get());
            } catch (InterruptedException e) {
            }
            return HealthcheckStatus.healthy;
        }
    }
}