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

import com.flipkart.ranger.healthcheck.HealthChecker;
import com.flipkart.ranger.healthcheck.Healthcheck;
import com.flipkart.ranger.healthservice.ServiceHealthAggregator;
import com.flipkart.ranger.model.Serializer;
import com.flipkart.ranger.model.ServiceNode;
import com.github.rholder.retry.*;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

public class ServiceProvider<T> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProvider.class);

    private String serviceName;
    private Serializer<T> serializer;
    private CuratorFramework curatorFramework;
    private ServiceNode<T> serviceNode;
    private List<Healthcheck> healthchecks;
    private final int refreshInterval;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> future;
    private ServiceHealthAggregator serviceHealthAggregator;


    public ServiceProvider(String serviceName, Serializer<T> serializer,
                           CuratorFramework curatorFramework,
                           ServiceNode<T> serviceNode,
                           List<Healthcheck> healthchecks,
                           int refreshInterval,
                           ServiceHealthAggregator serviceHealthAggregator) {
        this.serviceName = serviceName;
        this.serializer = serializer;
        this.curatorFramework = curatorFramework;
        this.serviceNode = serviceNode;
        this.healthchecks = healthchecks;
        this.refreshInterval = refreshInterval;
        this.serviceHealthAggregator = serviceHealthAggregator;
    }

    public void updateState(ServiceNode<T> serviceNode) throws Exception {
        final String path = String.format("/%s/%s", serviceName, serviceNode.representation());
        if(null == curatorFramework.checkExists().forPath(path)) {
            createPath();
        }
        curatorFramework.setData().forPath(
                path,
                serializer.serialize(serviceNode));
    }

    public void start() throws Exception {
        serviceHealthAggregator.start();
        curatorFramework.blockUntilConnected();
        curatorFramework.newNamespaceAwareEnsurePath(String.format("/%s", serviceName)).ensure(curatorFramework.getZookeeperClient());
        logger.debug("Connected to zookeeper");
        createPath();
        logger.debug("Set initial node data on zookeeper.");
        future = executorService.scheduleWithFixedDelay(new HealthChecker<T>(healthchecks, this), 0, refreshInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() throws Exception {
        serviceHealthAggregator.stop();
        if(null != future) {
            future.cancel(true);
        }
        curatorFramework.close();
    }

    public ServiceNode<T> getServiceNode() {
        return serviceNode;
    }

    private void createPath() throws Exception {
        Retryer<Void> retryer = RetryerBuilder.<Void>newBuilder()
                .retryIfExceptionOfType(KeeperException.NodeExistsException.class) //Ephimeral node still exists
                .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
                .withBlockStrategy(BlockStrategies.threadSleepStrategy())
                .withStopStrategy(StopStrategies.neverStop())
                .build();
        try {
            retryer.call(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(
                            String.format("/%s/%s", serviceName, serviceNode.representation()),
                            serializer.serialize(serviceNode));
                    return null;
                }
            });
        } catch (Exception e) {
            logger.error("Could not create node after 60 retries (1 min). This service will not be discoverable. Retry after some time.", e);
            throw new Exception("Could not create node after 60 retries (1 min). This service will not be discoverable. Retry after some time.", e);
        }

    }
}
