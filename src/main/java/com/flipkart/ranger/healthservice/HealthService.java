package com.flipkart.ranger.healthservice;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.healthservice.monitor.HealthMonitor;

/**
 * An interface which can be used to track the health of any service
 * You can add a set of monitors, and a general health will be tracked by
 *
 * @see ServiceHealthAggregator
 */
public interface HealthService {

    /**
     * Register a monitor to the service, to setup a continuous monitoring on the monitor
     *
     * @param monitor an implementation of the {@link HealthMonitor}
     */
    void addMonitor(HealthMonitor monitor);

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
