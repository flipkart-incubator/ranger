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
import com.flipkart.ranger.healthservice.monitor.IsolatedHealthMonitor;

/**
 * A monitor that can be used as a counting monitor to check if any countable entity breaches a threashhold
 * Eg:
 * 1. this could be used to check the heap of your java service, if heap goes beyond a threshold,
 * after which, you would want your service to be marked as unhealthy
 * Just extend this class and implement {@link #getCount()},
 * where you could connect to the {@link javax.management.MBeanServer}and return the current heap space being consumed
 * 2. this could be used to keep track of the active thread count of the service
 *
 * @see DiskSpaceMonitor
 */
public abstract class CountMonitor extends IsolatedHealthMonitor {

    public enum CheckSign {
        LESSER_THAN,
        LESSER_THAN_EQUALTO,
        GREATER_THAN,
        GREATER_THAN_EQUALTO,
    }

    private Integer threshold;
    private CheckSign checkSign;

    /**
     * @param name       name of monitor
     * @param checkSign  {@link CheckSign}
     * @param threshold  threshold with which the current count will be checked
     * @param timeEntity how often the {@link #monitor()} check needs to be executed
     */
    public CountMonitor(String name, CheckSign checkSign, Integer threshold, TimeEntity timeEntity) {
        super(name, timeEntity);
        this.checkSign = checkSign;
        this.threshold = threshold;
    }

    @Override
    public HealthcheckStatus monitor() {
        final long count = getCount().longValue();
        switch (checkSign) {
            case LESSER_THAN:
                if (count < threshold) {
                    return HealthcheckStatus.healthy;
                } else {
                    return HealthcheckStatus.unhealthy;
                }
            case LESSER_THAN_EQUALTO:
                if (count <= threshold) {
                    return HealthcheckStatus.healthy;
                } else {
                    return HealthcheckStatus.unhealthy;
                }
            case GREATER_THAN:
                if (count > threshold) {
                    return HealthcheckStatus.healthy;
                } else {
                    return HealthcheckStatus.unhealthy;
                }
            default:
                if (count >= threshold) {
                    return HealthcheckStatus.healthy;
                } else {
                    return HealthcheckStatus.unhealthy;
                }
        }
    }

    /**
     * @return the current value of the entity being monitored
     */
    public abstract Number getCount();
}
