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

package com.flipkart.ranger.core.finder;

import com.flipkart.ranger.core.model.Deserializer;
import com.flipkart.ranger.core.model.NodeDataSource;
import com.flipkart.ranger.core.model.ServiceRegistry;
import com.flipkart.ranger.core.signals.Signal;
import com.flipkart.ranger.core.util.Exceptions;
import com.github.rholder.retry.RetryerBuilder;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ServiceRegistryUpdater<T, D extends Deserializer<T>> {

    private final ServiceRegistry<T> serviceRegistry;
    private final NodeDataSource<T,D> nodeDataSource;
    private final D deserializer;

    private Lock checkLock = new ReentrantLock();
    private Condition checkCondition = checkLock.newCondition();
    private boolean checkForUpdate = false;
    private Future<Void> queryThreadFuture;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private AtomicBoolean initialRefreshCompleted = new AtomicBoolean(false);

    public ServiceRegistryUpdater(
            ServiceRegistry<T> serviceRegistry,
            NodeDataSource<T,D> nodeDataSource,
            List<Signal<T>> signalGenerators,
            D deserializer) {
        this.serviceRegistry = serviceRegistry;
        this.nodeDataSource = nodeDataSource;
        this.deserializer = deserializer;
        signalGenerators.forEach(signalGenerator -> signalGenerator.registerConsumer(this::checkForUpdate));
    }

    public void start() {
        val serviceName = serviceRegistry.getService().getServiceName();
        queryThreadFuture = this.executorService.submit(this::queryExecutor);
        log.info("Started updater for [{}]. Triggering initial update.", serviceName);
        checkForUpdate(null);
        log.info("Waiting for initial update to complete for: {}", serviceName);
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
        log.info("Initial node list updated for service: {} in {}ms",
                    serviceName, stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    public void stop() {
        if(null != queryThreadFuture) {
            executorService.shutdownNow();
        }
    }

    public void checkForUpdate(T signalData) {
        Preconditions.checkArgument(null == signalData);
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
                log.info("Updater thread interrupted");
                Exceptions.illegalState(e);
            }
            catch (Exception e) {
                log.error("Registry update failed for service: " + serviceRegistry.getService().name(), e);
            }
            finally {
                checkForUpdate = false;
                checkLock.unlock();
            }
        }
    }

    private void updateRegistry() {
        log.debug("Checking for updates on data source for service: {}",
                     serviceRegistry.getService().getServiceName());
        if(!nodeDataSource.isActive()) {
            log.warn("Node data source seems to be down. Keeping old list for {}",
                        serviceRegistry.getService().getServiceName());
            return;
        }
        val nodes = nodeDataSource.refresh(deserializer).orElse(null);
        if (null != nodes) {
            log.debug("Updating nodelist of size: {} for [{}]", nodes.size(),
                         serviceRegistry.getService().getServiceName());
            serviceRegistry.updateNodes(nodes);
            initialRefreshCompleted.compareAndSet(false, true);
        }
        else {
            log.warn("Null list returned from node data source. We are in a weird state. Keeping old list for {}",
                        serviceRegistry.getService().getServiceName());
        }
    }

}
