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
 * @param <T>
 */
public class HealthChecker<T> implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HealthChecker.class);

    private List<Healthcheck> healthchecks;
    private ServiceProvider<T> serviceProvider;

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
        ServiceNode<T> serviceNode = serviceProvider.getServiceNode();
        serviceNode.setHealthcheckStatus(healthcheckStatus);
        serviceNode.setLastUpdatedTimeStamp(System.currentTimeMillis());
        try {
            serviceProvider.updateState(serviceNode);
            logger.debug(String.format("Node is %s for (%s, %d)",
                                        healthcheckStatus, serviceNode.getHost(), serviceNode.getPort()));
        } catch (Exception e) {
            logger.error("Error updating health state in zookeeper: ", e);
        }
    }
}
