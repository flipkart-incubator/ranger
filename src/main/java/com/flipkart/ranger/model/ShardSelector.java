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

package com.flipkart.ranger.model;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class ShardSelector<T, ServiceRegistryType extends ServiceRegistry<T>> {
    private int minNodesAvailability;

    public ShardSelector(int minNodesAvailability) {
        this.minNodesAvailability = minNodesAvailability;
    }

    public List<ServiceNode<T>> getServiceableNodes(List<ServiceNode<T>> nodes) {
        final long zombieCheckThresholdTime = System.currentTimeMillis() - 60000; //1 Minute
        List<ServiceNode<T>> serviceableNodes = Lists.newArrayListWithCapacity(nodes.size());
        List<ServiceNode<T>> unhealthyNodes = Lists.newArrayListWithCapacity(nodes.size());

        minNodesAvailability = (minNodesAvailability == 0 ? 1 : minNodesAvailability);

        for (ServiceNode<T> node: nodes) {
            if (HealthcheckStatus.healthy == node.getHealthcheckStatus() && node.getLastUpdatedTimeStamp() > zombieCheckThresholdTime) {
                serviceableNodes.add(node);
            } else if (HealthcheckStatus.unhealthy == node.getHealthcheckStatus() && node.getLastUpdatedTimeStamp() > zombieCheckThresholdTime) {
                unhealthyNodes.add(node);
            }
        }
        int randomUnhealthyNode;
        while (serviceableNodes.size() < minNodesAvailability && unhealthyNodes.size() > 0) {
            randomUnhealthyNode = ThreadLocalRandom.current().nextInt(unhealthyNodes.size());
            serviceableNodes.add(unhealthyNodes.get(randomUnhealthyNode));
            unhealthyNodes.remove(randomUnhealthyNode);
        }

        return serviceableNodes;
    }

    public abstract List<ServiceNode<T>> nodes(T criteria, ServiceRegistryType serviceRegistry);
}
