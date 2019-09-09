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

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.PathBuilder;
import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.model.ServiceRegistry;
import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceRegistryUpdater<T> implements Callable<Void> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryUpdater.class);

    private ServiceRegistry<T> serviceRegistry;
    private final boolean disableWatchers;

    private Lock checkLock = new ReentrantLock();
    private Condition checkCondition = checkLock.newCondition();
    private boolean checkForUpdate = false;

    public ServiceRegistryUpdater(ServiceRegistry<T> serviceRegistry, boolean disableWatchers) {
        this.serviceRegistry = serviceRegistry;
        this.disableWatchers = disableWatchers;
    }

    public void start() throws Exception {
        CuratorFramework curatorFramework = serviceRegistry.getService().getCuratorFramework();
        if(!disableWatchers) {
            curatorFramework.getChildren()
                    .usingWatcher(new CuratorWatcher() {
                        @Override
                        public void process(WatchedEvent event) throws Exception {
                            switch (event.getType()) {

                                case NodeChildrenChanged: {
                                    checkForUpdate();
                                    break;
                                }
                                case None:
                                case NodeCreated:
                                case NodeDeleted:
                                case NodeDataChanged:
                                default:
                                    break;
                            }
                        }
                    })
                    .forPath(PathBuilder.path(serviceRegistry.getService())); //Start watcher on service node
        }
        updateRegistry();
        logger.info("Started polling zookeeper for changes for service:{}", serviceRegistry.getService().getServiceName());
    }

    @Override
    public Void call() throws Exception {
        //Start checking for updates
        while (true) {
            try {
                checkLock.lock();
                while (!checkForUpdate) {
                    checkCondition.await();
                }
                updateRegistry();
                checkForUpdate =false;
            } finally {
                checkLock.unlock();
            }
        }
    }

    public void checkForUpdate() {
        try {
            checkLock.lock();
            checkForUpdate = true;
            checkCondition.signalAll();
        } finally {
            checkLock.unlock();
        }
    }

    private Optional<List<ServiceNode<T>>> checkForUpdateOnZookeeper() {
        try {
            final long healthcheckZombieCheckThresholdTime = System.currentTimeMillis() - 60000; //1 Minute
            final Service service = serviceRegistry.getService();
            final String serviceName = service.getServiceName();
            if(!service.isRunning()) {
                return Optional.empty();
            }
            final Deserializer<T> deserializer = serviceRegistry.getDeserializer();
            final CuratorFramework curatorFramework = service.getCuratorFramework();
            final String parentPath = PathBuilder.path(service);
            logger.debug("Looking for node list of [{}]", serviceName);
            List<String> children = curatorFramework.getChildren().forPath(parentPath);
            List<ServiceNode<T>> nodes = Lists.newArrayListWithCapacity(children.size());
            logger.debug("Found {} nodes for [{}]", children.size(), serviceName);
            for(String child : children) {
                final String path = String.format("%s/%s", parentPath, child);
                boolean hasChild = null != curatorFramework.checkExists().forPath(path);
                final byte[] data = hasChild ? curatorFramework.getData().forPath(path) : null;
                if(null == data) {
                    logger.warn("Not data present for node: {} of [{}]", path, serviceName);
                    continue;
                }
                ServiceNode<T> key = deserializer.deserialize(data);
                if (HealthcheckStatus.healthy == key.getHealthcheckStatus()){
                    if (key.getLastUpdatedTimeStamp() > healthcheckZombieCheckThresholdTime){
                        nodes.add(key);
                    }
                    else {
                        logger.warn("Zombie node [{}:{}] found for [{}]", key.getHost(), key.getPort(), serviceName);
                    }
                }
            }
            return Optional.of(nodes);
        } catch (Exception e) {
            logger.error("Error getting service data from zookeeper: ", e);
        }
        return Optional.empty();
    }

    public void stop() {
        logger.info("Stopped updater for [{}]", serviceRegistry.getService().getServiceName());
    }


    private void updateRegistry() {
        List<ServiceNode<T>> nodes = checkForUpdateOnZookeeper().orElse(null);
        if(null != nodes) {
            logger.debug("Updating nodelist of size: {} for [{}]", nodes.size(),
                    serviceRegistry.getService().getServiceName());
            serviceRegistry.nodes(nodes);
        }
        else {
            logger.warn("No service shards/nodes found. We are disconnected from zookeeper. Keeping old list for {}",
                        serviceRegistry.getService().getServiceName());
        }
    }

}
