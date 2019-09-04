/**
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
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

package com.flipkart.ranger.healthcheck;

import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.serviceprovider.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A Runnable which maintains the health state of the ServiceProvider
 * @param <T> type of provider
 */
public class HealthChecker<T> implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HealthChecker.class);

    private List<Healthcheck> healthchecks;
    private ServiceProvider<T> serviceProvider;
    private HealthcheckStatus lastHealthcheckStatus;
    private long lastUpdatedTime;

    public HealthChecker(List<Healthcheck> healthchecks, ServiceProvider<T> serviceProvider) {
        this.healthchecks = healthchecks;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void run() {
        HealthcheckStatus healthcheckStatus = HealthcheckStatus.unhealthy;
        for(Healthcheck healthcheck : healthchecks) {
            try {
                healthcheckStatus = healthcheck.check();
            } catch (Throwable t) {
                logger.error("Error running healthcheck. Setting node to unhealthy", t);
                healthcheckStatus = HealthcheckStatus.unhealthy;
            }
            if(HealthcheckStatus.unhealthy == healthcheckStatus) {
                break;
            }
        }
        //Trigger update only if state change has happened
        //Conditions on which update will be triggered
        //1. First time
        //2. Stale update threshold breach
        //3. Update in health status
        if(lastHealthcheckStatus == null ||
            (System.currentTimeMillis() - lastUpdatedTime) > serviceProvider.getStaleUpdateThreshold()
                || lastHealthcheckStatus != healthcheckStatus) {
            lastUpdatedTime = System.currentTimeMillis();
            ServiceNode<T> serviceNode = serviceProvider.getServiceNode();
            serviceNode.setHealthcheckStatus(healthcheckStatus);
            serviceNode.setLastUpdatedTimeStamp(lastUpdatedTime);
            try {
                serviceProvider.updateState(serviceNode);
                logger.debug("Node is {} for ({}, {})",
                    healthcheckStatus.name(), serviceNode.getHost(), serviceNode.getPort());
            } catch (Exception e) {
                logger.error("Error updating health state in zookeeper: ", e);
            }
        }
        lastHealthcheckStatus = healthcheckStatus;
    }
}
