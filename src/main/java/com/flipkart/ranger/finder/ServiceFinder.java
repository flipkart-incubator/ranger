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

package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.model.ServiceNodeSelector;
import com.flipkart.ranger.model.ServiceRegistry;
import com.flipkart.ranger.model.ShardSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

public class ServiceFinder<T, ServiceRegistryType extends ServiceRegistry<T>> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceFinder.class);

    private final ServiceRegistryType serviceRegistry;
    private final ShardSelector<T, ServiceRegistryType> shardSelector;
    private AbstractServiceRegistryUpdater<T> updater;
    private final ServiceNodeSelector<T> nodeSelector;
    private int refreshIntervalMillis;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;

    public ServiceFinder(ServiceRegistryType serviceRegistry, AbstractServiceRegistryUpdater<T> updater, ShardSelector<T, ServiceRegistryType> shardSelector, ServiceNodeSelector<T> nodeSelector, int refreshIntervalMillis) {
        this.serviceRegistry = serviceRegistry;
        this.shardSelector = shardSelector;
        this.nodeSelector = nodeSelector;
        this.updater = updater;
        this.refreshIntervalMillis = refreshIntervalMillis;
    }

    public ServiceNode<T> get(T criteria) {
        List<ServiceNode<T>> nodes = shardSelector.nodes(criteria, serviceRegistry);
        if(null == nodes || nodes.isEmpty()) {
            return null;
        }
        return nodeSelector.select(nodes);
    }

    public List<ServiceNode<T>> getAll(T criteria) {
        return shardSelector.nodes(criteria, serviceRegistry);
    }

    public void start() throws Exception {
        //TODO: verify
        updater.setServiceRegistry(serviceRegistry);

        updater.start();

        executorService.submit(updater);
        scheduledFuture = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    updater.checkForUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, refreshIntervalMillis, TimeUnit.MILLISECONDS);
        logger.debug("ServiceFinder Scheduler Started");
    }

    public void stop() throws Exception {
        updater.stop();
        try {
            if( null != scheduledFuture ) {
                scheduledFuture.cancel(true);
            }
            updater.stop();
        } catch (Exception e) {
            logger.error("Error stopping ZK poller: ", e);
        }
        logger.debug("ServiceFinder Scheduler stopped");
    }
}
