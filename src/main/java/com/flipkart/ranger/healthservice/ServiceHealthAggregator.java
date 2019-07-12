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

import com.flipkart.ranger.healthcheck.Healthcheck;
import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.healthservice.monitor.IsolatedHealthMonitor;
import com.flipkart.ranger.healthservice.monitor.Monitor;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is a simple {@link HealthService} implementation,
 * which can be used to register a set of {@link IsolatedHealthMonitor}s and get an aggregated health of the service.
 * The aggregated health is maintained by scheduling and running the set of registered (enabled) monitors at regular intervals.
 */
public class ServiceHealthAggregator implements HealthService<HealthcheckStatus>, Healthcheck {

    /* Logger */
    private static final Logger logger = LoggerFactory.getLogger(ServiceHealthAggregator.class.getSimpleName());

    /* An atomic reference of the aggregated health */
    private AtomicReference<HealthcheckStatus> healthcheckStatusAtomicReference;

    /* List of all futures of scheduled monitors */
    private List<ScheduledFuture<?>> scheduledFutureList;

    /* List of all registered inline monitors */
    private List<Monitor<HealthcheckStatus>> inlineHealthMonitorList;

    /* List of all registered isolated monitors */
    private List<IsolatedHealthMonitor> isolatedHealthMonitorList;

    /* If aggregator is running or not */
    private AtomicBoolean running;

    /* Start time of checks .. will be used to ignore checks that are not yet supposed to start */
    private long startTime;

    public ServiceHealthAggregator() {
        this.healthcheckStatusAtomicReference = new AtomicReference<>();
        this.isolatedHealthMonitorList = Lists.newArrayList();
        this.inlineHealthMonitorList = Lists.newArrayList();
        this.running = new AtomicBoolean(false);
        this.startTime = 0;
    }

    /**
     * this method can be used to add a {@link IsolatedHealthMonitor} which will later be
     * scheduled at regular intervals and monitored to generate and maintain an aggregated health of the service
     * the scheduling will happen in an isolated thread
     *
     * @param monitor any extension of the {@link IsolatedHealthMonitor}
     */
    @Override
    public void addIsolatedMonitor(IsolatedHealthMonitor monitor) {
        if (running.get()) {
            /* cant add monitors when the Aggregator is already running */
            throw new UnsupportedOperationException("Cannot add a monitor when Aggregator is running");
        }
        isolatedHealthMonitorList.add(monitor);
    }

    /**
     * this method can be used to add a {@link Monitor}
     * this monitor will not be scheduled in a separate isolated thread,
     * but instead its execution will happen inline, in a single thread, along with other inline monitors
     *
     * @param monitor an implementation of line {@link Monitor}
     */
    @Override
    public void addInlineMonitor(Monitor<HealthcheckStatus> monitor) {
        if (running.get()) {
            /* cant add monitors when the Aggregator is already running */
            throw new UnsupportedOperationException("Cannot add a monitor when Aggregator is running");
        }
        inlineHealthMonitorList.add(monitor);
    }

    /**
     * start monitoring all registered monitors
     * (triggers a scheduled execution of all registered monitors) and saves their futures for later reference)
     */
    @Override
    public void start() {
        if (running.get()) {
            /* in case the aggregator is already running */
            logger.info("Service aggregator is already running");
            return;
        }
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(isolatedHealthMonitorList.size());
        scheduledFutureList = Lists.newArrayListWithCapacity(isolatedHealthMonitorList.size());
        for (IsolatedHealthMonitor isolatedHealthMonitor : isolatedHealthMonitorList) {
            final ScheduledFuture<?> scheduledFuture =
                    scheduledExecutorService.scheduleWithFixedDelay(
                            isolatedHealthMonitor,
                            isolatedHealthMonitor.getRunInterval()
                                    .getInitialDelay(),
                            isolatedHealthMonitor.getRunInterval()
                                    .getTimeInterval(),
                            isolatedHealthMonitor.getRunInterval()
                                    .getTimeUnit());
            scheduledFutureList.add(scheduledFuture);
        }
        startTime = System.currentTimeMillis();
        running.set(true);
    }

    /**
     * stop all running monitors
     */
    @Override
    public synchronized void stop() {
        if (running.get()) {
            logger.error("Service aggregator is currently not running, cannot stop..");
            return;
        }
        for (ScheduledFuture<?> scheduledFuture : scheduledFutureList) {
            scheduledFuture.cancel(true);
        }
        running.set(false);
    }

    /**
     * aggregate all registered (enabled) monitors, collect individual monitor healths, and aggregate them accordingly
     *
     * @return the aggregated health of the service
     */
    @Override
    public HealthcheckStatus getServiceHealth() {
        if (!running.get()) {
            throw new UnsupportedOperationException("Cannot get HealthStatus, when Aggregator isnt running. " +
                                                            "Please start the aggregator before trying to get health");
        }
        healthcheckStatusAtomicReference.set(HealthcheckStatus.healthy);
        Date currentTime = new Date();

        /* check health status of isolated monitors */
        final boolean hasUnhealthyMonitor = isolatedHealthMonitorList.stream()
                .filter(this::isMonitorReady)
                .filter(isolatedHealthMonitor -> !isolatedHealthMonitor.isDisabled())
                .anyMatch(isolatedHealthMonitor -> isIsolatedMonitorHealthy(isolatedHealthMonitor, currentTime));
        if (hasUnhealthyMonitor) {
            healthcheckStatusAtomicReference.set(HealthcheckStatus.unhealthy);
        }
        processMonitors();

        return healthcheckStatusAtomicReference.get();
    }

    @Override
    public HealthcheckStatus check() {
        return getServiceHealth();
    }

    private boolean isIsolatedMonitorHealthy( IsolatedHealthMonitor isolatedHealthMonitor, Date currentTime) {
        if (HealthcheckStatus.unhealthy == isolatedHealthMonitor.getHealthStatus()) {
            return true;
        }
        final boolean hasValidUpdateTime
                = null != isolatedHealthMonitor.getLastStatusUpdateTime()
                && hasValidUpdatedTime(isolatedHealthMonitor, currentTime);
            /* check if the monitor and its last updated time is stale, if so, mark status as unhealthy */
        if (!hasValidUpdateTime) {
            logger.error("Monitor: {} is stuck and its status is stale. Marking service as unhealthy",
                         isolatedHealthMonitor.getName());
            return true;
        }
        return false;
    }

    private boolean hasValidUpdatedTime(IsolatedHealthMonitor isolatedHealthMonitor, Date currentTime) {
        final long timeDifferenceMillis = currentTime.getTime() - isolatedHealthMonitor.getLastStatusUpdateTime()
                .getTime();
        return timeDifferenceMillis <= isolatedHealthMonitor.getStalenessAllowedInMillis();
    }

    private boolean isMonitorReady(IsolatedHealthMonitor isolatedHealthMonitor) {
        final TimeEntity runInterval = isolatedHealthMonitor.getRunInterval();
        final long initialDelay = runInterval.getInitialDelay();
        if(0 == initialDelay) {
            return true;
        }
        final long delayEndTime = startTime + runInterval.getTimeUnit().toMillis(runInterval.getInitialDelay());
        final long currentTimeMillis = System.currentTimeMillis();
        if( currentTimeMillis > delayEndTime ) {
            return true;
        }
        logger.warn("Ignoring monitor {} as current time {} is still before ready time {}",
                    isolatedHealthMonitor.getName(), new Date(delayEndTime), new Date(currentTimeMillis));
        return false;
    }

    private void processMonitors() {
    /* check status of all inline monitors in the same thread */
        for (Monitor<HealthcheckStatus> healthMonitor : inlineHealthMonitorList) {
            if (healthMonitor.isDisabled()) {
                continue;
            }
            final HealthcheckStatus monitorStatus = healthMonitor.monitor();
            if (HealthcheckStatus.unhealthy == monitorStatus) {
                healthcheckStatusAtomicReference.set(HealthcheckStatus.unhealthy);
            }
        }
    }
}
