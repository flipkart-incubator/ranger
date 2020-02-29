package com.flipkart.ranger.core.util;

import com.flipkart.ranger.core.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
public class FinderUtils {

    private FinderUtils() {}

    public static<T> List<ServiceNode<T>> filterValidNodes(
            final Service service,
            final Collection<ServiceNode<T>> serviceNodes,
            long healthcheckZombieCheckThresholdTime) {
        return serviceNodes.stream()
                .filter(serviceNode -> isValidNode(service, healthcheckZombieCheckThresholdTime, serviceNode))
                .collect(Collectors.toList());
    }

    public static <T> boolean isValidNode(
            Service service,
            long healthcheckZombieCheckThresholdTime,
            ServiceNode<T> serviceNode) {
        if(HealthcheckStatus.healthy != serviceNode.getHealthcheckStatus()) {
            log.debug("Unhealthy node [{}:{}] found for [{}]",
                      serviceNode.getHost(), serviceNode.getPort(), service.getServiceName());
            return false;
        }
        if(serviceNode.getLastUpdatedTimeStamp() < healthcheckZombieCheckThresholdTime) {
            log.warn("Zombie node [{}:{}] found for [{}]",
                      serviceNode.getHost(), serviceNode.getPort(), service.getServiceName());
            return false;
        }
        return true;
    }
}
