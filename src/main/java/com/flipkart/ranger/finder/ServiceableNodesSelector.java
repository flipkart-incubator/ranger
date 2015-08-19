package com.flipkart.ranger.finder;

import com.flipkart.ranger.finder.unsharded.UnshardedClusterInfo;
import com.flipkart.ranger.finder.unsharded.UnshardedClusterServiceRegistry;
import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.model.ServiceNode;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by udit.jain on 19/08/15.
 */
public class ServiceableNodesSelector<T> {

    int minNodesAvailablePercentage;

    public ServiceableNodesSelector(int minNodesAvailablePercentage) {
        this.minNodesAvailablePercentage = minNodesAvailablePercentage;
    }

    public List<ServiceNode<T>> getServiceableNodes(List<ServiceNode<T>> nodes) {
        List<ServiceNode<T>> serviceableNodes = Lists.newArrayListWithCapacity(nodes.size());
        List<ServiceNode<T>> unhealthyNodes = Lists.newArrayListWithCapacity(nodes.size());

        int minNodes = (int) (nodes.size() * minNodesAvailablePercentage) / 100;
        minNodes = (minNodes == 0 ? 1 : minNodes);

        for (ServiceNode<T> node: nodes) {
            if (HealthcheckStatus.healthy == node.getHealthcheckStatus()) {
                serviceableNodes.add(node);
            }else if (HealthcheckStatus.unhealthy == node.getHealthcheckStatus()) {
                unhealthyNodes.add(node);
            }
        }
        int randomUnhealthyNode;
        while (serviceableNodes.size() < minNodes && unhealthyNodes.size() > 0) {
            randomUnhealthyNode = ThreadLocalRandom.current().nextInt(unhealthyNodes.size());
            serviceableNodes.add(unhealthyNodes.get(randomUnhealthyNode));
            unhealthyNodes.remove(randomUnhealthyNode);
        }

        return serviceableNodes;
    }
}
