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

import com.flipkart.ranger.datasource.NodeDataSource;
import com.flipkart.ranger.datasource.RegistryUpdateSignalGenerator;
import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.model.ServiceRegistry;
import com.flipkart.ranger.util.Exceptions;
import com.github.rholder.retry.RetryerBuilder;
import com.google.common.base.Stopwatch;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceRegistryUpdater<T> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryUpdater.class);

    private final ServiceRegistry<T> serviceRegistry;
    private final NodeDataSource<T> nodeDataSource;

    private Lock checkLock = new ReentrantLock();
    private Condition checkCondition = checkLock.newCondition();
    private boolean checkForUpdate = false;
    private final List<RegistryUpdateSignalGenerator<T>> signalGenerators;
    private Future<Void> queryThreadFuture;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private AtomicBoolean initialRefreshCompleted = new AtomicBoolean(false);

    public ServiceRegistryUpdater(
            ServiceRegistry<T> serviceRegistry,
            NodeDataSource<T> nodeDataSource,
            List<RegistryUpdateSignalGenerator<T>> signalGenerators) {
        this.serviceRegistry = serviceRegistry;
        this.nodeDataSource = nodeDataSource;
        this.signalGenerators = signalGenerators;
        signalGenerators.forEach(signalGenerator -> signalGenerator.registerConsumer(this::checkForUpdate));
    }

    public void start() {
        val serviceName = serviceRegistry.getService().getServiceName();
        nodeDataSource.start();
        logger.info("Started data source for [{}]", serviceName);
        this.signalGenerators.forEach(RegistryUpdateSignalGenerator::start);
        logger.info("Started signal generators for [{}]", serviceName);
        queryThreadFuture = this.executorService.submit(this::queryExecutor);
        logger.info("Started updater for [{}]. Triggering initial update.", serviceName);
        checkForUpdate();
        logger.info("Waiting for initial update to complete for: {}", serviceName);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            RetryerBuilder.<Boolean>newBuilder()
                    .retryIfResult(r -> null == r || !r)
                .build()
            .call(initialRefreshCompleted::get);
        }
        catch (Exception e) {
            Exceptions.illegalState("Could not perform initial state for service: " + serviceName, e);
        }
        logger.info("Initial node list updated for service: {} in {}ms",
                    serviceName, stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    public void stop() {
        if(null != queryThreadFuture) {
            executorService.shutdownNow();
        }
        val serviceName = serviceRegistry.getService().getServiceName();
        this.signalGenerators.forEach(RegistryUpdateSignalGenerator::shutdown);
        logger.info("Stopped signal generators and updater for [{}]", serviceName);
        nodeDataSource.stop();
        logger.info("Stopped data source for [{}]", serviceName);
    }

    public void checkForUpdate() {
        try {
            checkLock.lock();
            checkForUpdate = true;
            checkCondition.signalAll();
        }
        finally {
            checkLock.unlock();
        }
    }

    private Void queryExecutor() {
        //Start checking for updates
        while (true) {
            try {
                checkLock.lock();
                while (!checkForUpdate) {
                    checkCondition.await();
                }
                updateRegistry();
            }
            catch (InterruptedException e) {
                logger.info("Updater thread interrupted");
            }
            finally {
                checkForUpdate = false;
                checkLock.unlock();
            }
        }
    }

    private void updateRegistry() {
        logger.debug("Checking for updates on data source for service: {}",
                     serviceRegistry.getService().getServiceName());
        if(!nodeDataSource.isActive()) {
            logger.warn("Node data source seems to be down. Keeping old list for {}",
                        serviceRegistry.getService().getServiceName());
            return;
        }
        List<ServiceNode<T>> nodes = nodeDataSource.refresh().orElse(null);
        if (null != nodes) {
            logger.debug("Updating nodelist of size: {} for [{}]", nodes.size(),
                         serviceRegistry.getService().getServiceName());
            serviceRegistry.updateNodes(nodes);
            initialRefreshCompleted.compareAndSet(false, true);
        }
        else {
            logger.warn("Null list returned from node data source. We are in a weird state. Keeping old list for {}",
                        serviceRegistry.getService().getServiceName());
        }
    }

}
