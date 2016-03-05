package com.flipkart.ranger.healthservice.monitor;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.healthservice.TimeEntity;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A health monitor that implements a {@link Monitor<HealthcheckStatus>} and {@link Runnable}
 * Maintains the latest state of the Monitor, and its latest updated timestamp
 *
 * @see com.flipkart.ranger.healthservice.monitor.sample.PingCheckMonitor
 * @see com.flipkart.ranger.healthservice.monitor.sample.RotationStatusMonitor
 * @see com.flipkart.ranger.healthservice.monitor.sample.CountMonitor
 */
public abstract class IsolatedHealthMonitor implements Runnable, Monitor<HealthcheckStatus> {

    /* name of the monitor */
    protected String name;

    /* reference of the health that this monitor tracks */
    private AtomicReference<HealthcheckStatus> healthStatus;

    /* reference to when this monitor ran successfully */
    private Date lastStatusUpdateTime;

    /* how often should this monitor run */
    private TimeEntity runInterval;

    /* reference to if this monitor is disabled or not (default: false) */
    private final AtomicBoolean disabled = new AtomicBoolean(false);

    /* after how long (in milliseconds) should the monitor be regarded as stale (default: 60 seconds) */
    private long stalenessAllowedInMillis;

    /**
     * @param name       name of the monitor
     * @param runInterval initial delay, time interval of how regularly the monitor is to be run, and timeunit
     *                   to specify how often the {@link #monitor()} check needs to be executed
     */
    public IsolatedHealthMonitor(String name, TimeEntity runInterval) {
        this(name, runInterval, 60000);
    }

    /**
     * @param name                     name of the monitor
     * @param runInterval               initial delay, time interval of how regularly the monitor is to be run, and timeunit
     *                                 to specify how often the {@link #monitor()} check needs to be executed
     * @param stalenessAllowedInMillis after how long (in milliseconds) should the monitor be regarded as stale (default: 60 seconds)
     */
    public IsolatedHealthMonitor(String name, TimeEntity runInterval, long stalenessAllowedInMillis) {
        this.name = name;
        this.stalenessAllowedInMillis = stalenessAllowedInMillis;
        this.healthStatus = new AtomicReference<>(HealthcheckStatus.healthy);
        this.runInterval = runInterval;
        this.disabled.set(false);
    }

    /**
     * updates the healthStatus with the latest value from the monitor check
     * also updates the {@code lastStatusUpdateTime}
     */
    @Override
    public void run() {
        healthStatus.set(monitor());
        lastStatusUpdateTime = new Date();
    }

    /**
     * disable the monitor, and dont use this monitor to track the aggregated health of the system
     * monitor is enabled by default
     */
    public void disable() {
        disabled.set(true);
    }

    /**
     * enable the monitor, and consider it while aggregating the health of the system
     * monitor is enabled by default
     */
    public void enable() {
        disabled.set(false);
    }

    public TimeEntity getRunInterval() {
        return runInterval;
    }

    public HealthcheckStatus getHealthStatus() {
        return healthStatus.get();
    }

    public Date getLastStatusUpdateTime() {
        return lastStatusUpdateTime;
    }

    public String getName() {
        return name;
    }

    /**
     * @return default of 60 seconds if not set
     */
    public long getStalenessAllowedInMillis() {
        return stalenessAllowedInMillis;
    }

    @Override
    public boolean isDisabled() {
        return disabled.get();
    }
}
