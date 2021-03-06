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

import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.PathBuilder;
import com.flipkart.ranger.model.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public abstract class AbstractZookeeperServiceRegistry<T> extends ServiceRegistry<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractZookeeperServiceRegistry.class);
    private final int refreshIntervalMillis;
    private final boolean disableWatchers;
    private ServiceRegistryUpdater<T> updater;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;
    private Future<Void> queryThreadFuture;

    protected AbstractZookeeperServiceRegistry(
            Service service,
            Deserializer<T> deserializer,
            int refreshIntervalMillis,
            boolean disableWatchers) {
        super(service, deserializer);
        this.refreshIntervalMillis = refreshIntervalMillis;
        this.disableWatchers = disableWatchers;
    }

    @Override
    public void start() throws Exception {
        final Service service = getService();
        service.getCuratorFramework().blockUntilConnected();
        logger.debug("Connected to zookeeper cluster for {}", service.getServiceName());
        service.getCuratorFramework().newNamespaceAwareEnsurePath(PathBuilder.path(service))
                                    .ensure(service.getCuratorFramework().getZookeeperClient());
        updater = new ServiceRegistryUpdater<>(this, disableWatchers);
        updater.start();
        queryThreadFuture = executorService.submit(updater);
        scheduledFuture = scheduler.scheduleWithFixedDelay(() -> {
            try {
                updater.checkForUpdate();
            } catch (Exception e) {
                logger.error("Error checking for updates from zk for service:" + service.getServiceName() , e);
            }
        }, 0, refreshIntervalMillis, TimeUnit.MILLISECONDS);
        logger.debug("Service Registry Started for {}", service.getServiceName());
    }

    @Override
    public void stop() throws Exception {
        final Service service = getService();
        try {
            if( null != scheduledFuture ) {
                scheduledFuture.cancel(true);
            }
            updater.stop();
            if(null != queryThreadFuture) {
                executorService.shutdownNow();
            }
        } catch (Exception e) {
            logger.error(String.format("Error stopping ZK poller for %s", service.getServiceName()), e);
        }
        service.getCuratorFramework().close();
        //TODO
        logger.debug("Service Registry stopped for {}", service.getServiceName());
    }

}
