package com.flipkart.ranger.healthservice;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.healthservice.monitor.HealthMonitor;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is a simple {@link HealthService} implementation,
 * which can be used to register a set of {@link HealthMonitor}s and get an aggregated health of the service.
 * The aggregated health is maintained by scheduling and running the set of registered (enabled) monitors at regular intervals.
 */
public class ServiceHealthAggregator implements HealthService {

    /* Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceHealthAggregator.class.getSimpleName());

    /* An atomic reference of the aggregated health */
    private AtomicReference<HealthcheckStatus> healthcheckStatusAtomicReference;

    /* Total number of registered monitors */
    private final AtomicInteger numMonitors = new AtomicInteger(0);

    /* List of all futures of scheduled monitors */
    private List<ScheduledFuture<?>> scheduledFutureList;

    /* List of all registered monitors */
    private List<HealthMonitor> healthMonitorList;

    /* If aggregator is running or not */
    private AtomicBoolean running;

    public ServiceHealthAggregator() {
        this.healthcheckStatusAtomicReference = new AtomicReference<>();
        this.healthMonitorList = Lists.newArrayList();
        this.running = new AtomicBoolean(false);
    }

    /**
     * this method can be used to add a {@link HealthMonitor} which will later be
     * scheduled at regular intervals and monitored to generate and maintain an aggregated health of the service
     *
     * @param monitor any extension of the {@link HealthMonitor}
     */
    @Override
    public void addMonitor(HealthMonitor monitor) {
        if (running.get()) {
            /* cant add monitors when the Aggregator is already running */
            throw new UnsupportedOperationException("Cannot add a monitor when Aggregator is running");
        }
        healthMonitorList.add(monitor);
        numMonitors.getAndIncrement();
    }

    /**
     * start monitoring all registered monitors
     * (triggers a scheduled execution of all registered monitors) and saves their futures for later reference)
     */
    @Override
    public void start() {
        if (running.get()) {
            /* in case the monitor is already running */
            return;
        }
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(numMonitors.get());
        scheduledFutureList = Lists.newArrayListWithCapacity(numMonitors.get());
        for (HealthMonitor healthMonitor : healthMonitorList) {
            final ScheduledFuture<?> scheduledFuture =
                    scheduledExecutorService.scheduleAtFixedRate(
                            healthMonitor,
                            healthMonitor.getTimeEntity().getInitialDelay(),
                            healthMonitor.getTimeEntity().getTimeInterval(),
                            healthMonitor.getTimeEntity().getTimeUnit());
            scheduledFutureList.add(scheduledFuture);
        }
        running.set(true);
    }

    /**
     * stop all running monitors
     */
    @Override
    public void stop() {
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
        for (HealthMonitor healthMonitor : healthMonitorList) {
            if (healthMonitor.isDisabled()) {
                continue;
            }
            Long timeDifference;
            if (null != healthMonitor.getLastStatusUpdateTime()) {
                timeDifference = currentTime.getTime() - healthMonitor.getLastStatusUpdateTime().getTime();
            } else {
                timeDifference = null;
            }
            /* check if the monitor and its last updated time is stale */
            if ((timeDifference == null || timeDifference > healthMonitor.getStalenessAllowedInMillis())
                    && HealthcheckStatus.unhealthy != healthcheckStatusAtomicReference.get()) {
                LOGGER.error("Monitor: {} is stuck and its status is stale. Marking service as unhealthy", healthMonitor.getName());
                healthcheckStatusAtomicReference.set(HealthcheckStatus.unhealthy);
            } else if (HealthcheckStatus.unhealthy == healthMonitor.getHealthStatus()) {
                healthcheckStatusAtomicReference.set(HealthcheckStatus.unhealthy);
            }
        }
        return healthcheckStatusAtomicReference.get();
    }
}
