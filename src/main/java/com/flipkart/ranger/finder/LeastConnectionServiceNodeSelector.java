/**
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
package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.model.ServiceNodeSelector;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class returns the service node having least number of active connections currently assigned to it.
 * It also takes care that nodes do not suffer from starvation.
 */
public class LeastConnectionServiceNodeSelector<T> implements ServiceNodeSelector<T> {

    private static final Logger log = LoggerFactory.getLogger(LeastConnectionServiceNodeSelector.class.getSimpleName());

    /**
     * ActiveNode is a ServiceNode to which at least one request has been sent in the past along with its current connection count
     */
    private static class ActiveNode<T> implements Comparable {

        private ServiceNode<T> serviceNode;
        private AtomicLong connectionCount;

        ActiveNode(ServiceNode<T> serviceNode, AtomicLong connectionCount) {
            this.serviceNode = serviceNode;
            this.connectionCount = connectionCount;
        }

        AtomicLong getConnectionCount() {
            return connectionCount;
        }

        ServiceNode<T> getServiceNode() {
            return serviceNode;
        }

        @Override
        public int compareTo(Object o) {
            if (connectionCount.get() < ((ActiveNode) o).getConnectionCount().get()) {
                return -1;
            }
            return 1;
        }

        public String toString() {
            return serviceNode.getHost() + ":" + serviceNode.getPort();
        }
    }

    @Override
    public ServiceNode<T> select(List<ServiceNode<T>> serviceNodes) {
        Map<ConnectionRequest, AtomicLong> activeConnections = ActiveConnectionMetrics.getActiveConnections();
        String correlationId = UUID.randomUUID().toString();
        log.debug("CorrelationId = {}, Active Connections {} = {}", correlationId, activeConnections);
        final List<ActiveNode<T>> activeNodes = Lists.newArrayList();
        for (ConnectionRequest connectionRequest : activeConnections.keySet()) {
            for (ServiceNode<T> serviceNode : serviceNodes) {
                if (serviceNode.getHost().equals(connectionRequest.getReplicaHostName()) &&
                        serviceNode.getPort() == connectionRequest.getPort()) {
                    ActiveNode<T> activeNode = new ActiveNode<>(serviceNode, activeConnections.get(connectionRequest));
                    activeNodes.add(activeNode);
                }
            }
        }

        // List of those service nodes to which request has never been sent.
        List<ServiceNode<T>> newServiceNodes = Lists.newLinkedList();
        for (ServiceNode<T> serviceNode : serviceNodes) {
            boolean found = false;
            for (ActiveNode<T> activeNode : activeNodes) {
                if (activeNode.getServiceNode().equals(serviceNode)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newServiceNodes.add(serviceNode);
            }
        }
        log.debug("CorrelationId = {}, ActiveNodes = {}, ServiceNodes = {}", correlationId, activeNodes, serviceNodes);
        log.debug("CorrelationId={}, ActiveNodesCount = {}, ServiceNodesCount = {}, NewNodesCount = {}",
                correlationId, activeNodes.size(), serviceNodes.size(), newServiceNodes.size());
        /*
         If there are a few service nodes to which connection request has never been sent in the past, we should first try
         to pick amongst those nodes before kicking off Least connection algorithm. This will ensure starvation of those nodes never happens.
         */
        if (!newServiceNodes.isEmpty()) {
            ServiceNode<T> serviceNode = newServiceNodes.get(ThreadLocalRandom.current().nextInt(newServiceNodes.size()));
            log.info("CorrelationId = {}, Randomly selected serviceNode = {}", correlationId, serviceNode);
            return serviceNode;
        }
        Collections.sort(activeNodes);
        ServiceNode<T> serviceNode = activeNodes.get(0).getServiceNode();
        log.info("CorrelationId = {}. Selected Node with Least connection = {}", correlationId, serviceNode);
        return serviceNode;
    }
}
