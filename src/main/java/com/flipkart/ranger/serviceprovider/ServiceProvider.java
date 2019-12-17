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

package com.flipkart.ranger.serviceprovider;

import com.flipkart.ranger.datasource.NodeDataSource;
import com.flipkart.ranger.finder.Service;
import com.flipkart.ranger.healthcheck.HealthChecker;
import com.flipkart.ranger.healthcheck.Healthcheck;
import com.flipkart.ranger.healthservice.ServiceHealthAggregator;
import com.flipkart.ranger.model.ServiceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ServiceProvider<T> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProvider.class);

    private final Service service;
    private final ServiceNode<T> serviceNode;
    private final List<Healthcheck> healthchecks;
    private final int healthUpdateInterval;
    private final int staleUpdateThreshold;
    private final NodeDataSource<T> dataSource;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> future;
    private ServiceHealthAggregator serviceHealthAggregator;


    public ServiceProvider(
            Service service,
            ServiceNode<T> serviceNode,
            List<Healthcheck> healthchecks,
            int healthUpdateInterval,
            int staleUpdateThreshold,
            NodeDataSource<T> dataSource,
            ServiceHealthAggregator serviceHealthAggregator) {
        this.service = service;
        this.serviceNode = serviceNode;
        this.healthchecks = healthchecks;
        this.healthUpdateInterval = healthUpdateInterval;
        this.staleUpdateThreshold = staleUpdateThreshold;
        this.dataSource = dataSource;
        this.serviceHealthAggregator = serviceHealthAggregator;
    }

    public void updateState(ServiceNode<T> serviceNode) {
        dataSource.updateState(serviceNode);
    }

    public void start() {
        dataSource.start();
        serviceHealthAggregator.start();
        logger.info("Connected to zookeeper for {}", service.getServiceName());
        dataSource.updateState(serviceNode);
        logger.debug("Set initial node data on zookeeper for {}", service.getServiceName());
        future = executorService.scheduleWithFixedDelay(new HealthChecker<>(healthchecks, this), 0,
                                                        healthUpdateInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        serviceHealthAggregator.stop();
        if(null != future) {
            future.cancel(true);
        }
        dataSource.stop();
    }

    public ServiceNode<T> getServiceNode() {
        return serviceNode;
    }

    public int getStaleUpdateThreshold() {
        return staleUpdateThreshold;
    }

}
