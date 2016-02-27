package com.flipkart.ranger.healthservice;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.healthservice.monitor.IsolatedHealthMonitor;
import com.flipkart.ranger.healthservice.monitor.Monitor;

/**
 * An interface which can be used to track the health of any service
 * You can add a set of monitors, and a general health will be tracked by
 *
 * @see ServiceHealthAggregator
 */
public interface HealthService {

    /**
     * Register a monitor to the service, to setup a continuous monitoring on the monitor
     * <p/>
     * this method can be used to add a {@link IsolatedHealthMonitor} which will later be
     * scheduled at regular intervals and monitored to generate and maintain an aggregated health of the service
     * the scheduling will happen in an isolated thread
     *
     * @param monitor an implementation of the {@link IsolatedHealthMonitor}
     */
    void addIsolatedMonitor(IsolatedHealthMonitor monitor);

    /**
     * Add a monitor which will be run in the same thread.
     * <p/>
     * this method can be used to add a {@link Monitor}
     * this monitor will not be scheduled in a separate isolated thread,
     * but instead its execution will happen inline, in a single thread, along with other inline monitors
     *
     * @param monitor an implementation of line {@link Monitor<HealthcheckStatus>}
     */
    void addInlineMonitor(Monitor<HealthcheckStatus> monitor);

    /**
     * Start monitoring all registered monitors.
     * Start individual monitors
     */
    void start();

    /**
     * Stop the health service, and stop tracking all monitors
     */
    void stop();

    /**
     * aggregate all registered (enabled) monitors, collect individual monitor healths, and aggregate them accordingly
     *
     * @return the aggregated health of the service
     */
    HealthcheckStatus getServiceHealth();
}
