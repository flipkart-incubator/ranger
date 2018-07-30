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
import com.flipkart.ranger.model.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractServiceRegistryUpdater<T> implements Callable<Void> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractServiceRegistryUpdater.class);

    public abstract void start() throws Exception;
    public abstract void stop() throws Exception;

    protected ServiceRegistry<T> serviceRegistry;

    private Lock checkLock = new ReentrantLock();
    private Condition checkCondition = checkLock.newCondition();
    private boolean checkForUpdate = false;

    public void setServiceRegistry(ServiceRegistry<T> serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public Void call() throws Exception {
        while (true) {
            try {
                checkLock.lock();
                while (!checkForUpdate) {
                    checkCondition.await();
                }
                List<ServiceNode<T>> nodes = getHealthyServiceNodes();
                if(null != nodes) {
                    logger.debug("Setting nodelist of size: " + nodes.size());
                    serviceRegistry.nodes(nodes);
                }
                else {
                    logger.warn("No service shards/nodes found. We are disconnected from zookeeper/http server. Keeping old list.");
                }
                checkForUpdate =false;
            } finally {
                checkLock.unlock();
            }
        }
    }

    protected abstract List<ServiceNode<T>> getHealthyServiceNodes();

    public void checkForUpdate() {
        try {
            checkLock.lock();
            checkForUpdate = true;
            checkCondition.signalAll();
        } finally {
            checkLock.unlock();
        }
    }
}
