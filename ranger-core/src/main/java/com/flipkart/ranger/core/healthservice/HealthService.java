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

package com.flipkart.ranger.core.healthservice;

import com.flipkart.ranger.core.healthservice.monitor.IsolatedHealthMonitor;
import com.flipkart.ranger.core.healthservice.monitor.Monitor;

/**
 * An interface which can be used to track the health of any service
 * You can add a set of monitors, and a general health will be tracked by
 *
 * @see ServiceHealthAggregator
 */
public interface HealthService<T> {

    /**
     * Register a monitor to the service, to setup a continuous monitoring on the monitor
     * this method can be used to add a {@link IsolatedHealthMonitor} which will later be
     * scheduled at regular intervals and monitored to generate and maintain an aggregated health of the service
     * the scheduling will happen in an isolated thread
     *
     * @param monitor an implementation of the {@link IsolatedHealthMonitor}
     */
    void addIsolatedMonitor(IsolatedHealthMonitor<T> monitor);

    /**
     * Add a monitor which will be run in the same thread.
     * this method can be used to add a {@link Monitor}
     * this monitor will not be scheduled in a separate isolated thread,
     * but instead its execution will happen inline, in a single thread, along with other inline monitors
     *
     * @param monitor an implementation of line {@link Monitor}
     */
    void addInlineMonitor(Monitor<T> monitor);

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
    T getServiceHealth();
}
