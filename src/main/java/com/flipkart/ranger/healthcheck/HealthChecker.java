package com.flipkart.ranger.healthcheck;

import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.serviceprovider.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
            healthcheckStatus = healthcheck.check();
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
