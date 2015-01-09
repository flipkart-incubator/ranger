package com.flipkart.ranger.healthcheck;

import com.flipkart.ranger.serviceprovider.ServiceProvider;
import com.flipkart.ranger.model.ServiceNode;

import java.util.List;

public class HealthChecker<T> implements Runnable {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
