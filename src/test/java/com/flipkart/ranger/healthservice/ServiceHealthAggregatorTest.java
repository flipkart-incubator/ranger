package com.flipkart.ranger.healthservice;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.healthservice.monitor.HealthMonitor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServiceHealthAggregatorTest {


    ServiceHealthAggregator serviceHealthAggregator = new ServiceHealthAggregator();
    TestMonitor testMonitor;
    @Before
    public void setUp() throws Exception {
        testMonitor = new TestMonitor("TestHealthMonitor", TimeEntity.EverySecond(), 1000);
        serviceHealthAggregator.addMonitor(testMonitor);

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

    }

    private class TestMonitor extends HealthMonitor {
        int threadSleep = 2000;

        public TestMonitor(String name, TimeEntity timeEntity) {
            super(name, timeEntity);
        }

        public TestMonitor(String name, TimeEntity timeEntity, long stalenessAllowedInMillis) {
            super(name, timeEntity, stalenessAllowedInMillis);
        }

        public void setThreadSleep(int threadSleep) {
            this.threadSleep = threadSleep;
        }

        @Override
        public synchronized HealthcheckStatus monitor() {
            try {
                Thread.sleep(threadSleep);
            } catch (InterruptedException e) {
            }
            return HealthcheckStatus.healthy;
        }
    }
}