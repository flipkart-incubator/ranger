package com.flipkart.ranger.zookeeper.zk;

import com.flipkart.ranger.core.model.NodeDataSource;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.core.model.Deserializer;
import com.flipkart.ranger.zookeeper.util.PathBuilder;
import com.flipkart.ranger.core.model.Serializer;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.util.Exceptions;
import com.github.rholder.retry.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
@Slf4j
public class ZkNodeDataSource<T> implements NodeDataSource<T> {

    private final Service service;
    private final CuratorFramework curatorFramework;
    private final Retryer<Boolean> discoveryRetrier = RetryerBuilder.<Boolean>newBuilder()
            .retryIfException(e -> IllegalStateException.class.isAssignableFrom(e.getClass()))
            .retryIfResult(aBoolean -> false)
            .withAttemptTimeLimiter(AttemptTimeLimiters.noTimeLimit())
            .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
            .withBlockStrategy(BlockStrategies.threadSleepStrategy())
            .withRetryListener(new RetryListener() {
                @Override
                public <V> void onRetry(Attempt<V> attempt) {
                    log.debug("Retrying with attempt: {}", attempt);
                }
            })
            .build();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    public ZkNodeDataSource(
            final Service service,
            final CuratorFramework curatorFramework) {
        this.service = service;
        this.curatorFramework = curatorFramework;
    }

    @Override
    public void start() {
        if (started.get()) {
            log.info("Start called on already initialized data source for service {}. Ignoring.",
                     service.getServiceName());
            return;
        }
        final String path = PathBuilder.servicePath(service);
        try {
            curatorFramework.blockUntilConnected();
            log.info("Connected to zookeeper cluster for {}", service.getServiceName());
            curatorFramework
                    .create()
                    .creatingParentContainersIfNeeded()
                    .forPath(path);
        }
        catch (KeeperException e) {
            if (e.code() == KeeperException.Code.NODEEXISTS) {
                log.info("Service node {} already exists for service: {}", path, service.getServiceName());
            }
        }
        catch (Exception e) {
            Exceptions.illegalState("Could not start ZK data source for service: " + service.getServiceName(), e);
        }
        started.set(true);
    }

    @Override
    public void ensureConnected() {
        try {
            discoveryRetrier.call(this::isActive);
        }
        catch (Exception e) {
            Exceptions.illegalState("Could not get zk connection", e);
        }
    }

    @Override
    public void stop() {
        if (stopped.get()) {
            log.info("Data source has already been stopped for: {}. Ignoring.", service.getServiceName());
            return;
        }
        if (!started.get()) {
            log.warn("Shutdown called for service: {}, but data source is not started.", service.getServiceName());
        }
        log.info("Shutting down data source for service: {}.  (It's a no-op.)",
                 service.getServiceName());
        stopped.set(true);
    }

    @Override
    public Optional<List<ServiceNode<T>>> refresh(Deserializer<T> deserializer) {
        return checkForUpdateOnZookeeper(deserializer);
    }

    @Override
    public boolean isActive() {
        return curatorFramework != null
                && (curatorFramework.getState() == CuratorFrameworkState.STARTED);
    }

    @Override
    public void updateState(Serializer<T> serializer, ServiceNode<T> serviceNode) {
        if (stopped.get()) {
            log.warn("Node has been stopped already for service: {}. No update will be possible.",
                     service.getServiceName());
            return;
        }
        Preconditions.checkNotNull(serializer, "Serializer has not been set for node data");
        final String path = PathBuilder.instancePath(service, serviceNode);
        try {
            if (null == curatorFramework.checkExists().forPath(path)) {
                log.info("No node exists for path: {}. Will create now.", path);
                createPath(serviceNode, serializer);
            }
            else {
                curatorFramework.setData().forPath(path, serializer.serialize(serviceNode));
            }
        }
        catch (Exception e) {
            log.error("Error updating node data at path " + path, e);
            Exceptions.illegalState(e);
        }
    }

    private synchronized void createPath(
            ServiceNode<T> serviceNode,
            Serializer<T> serializer) {
        final String instancePath = PathBuilder.instancePath(service, serviceNode);
        try {
            if (null == curatorFramework.checkExists().forPath(instancePath)) {
                curatorFramework.create()
                        .creatingParentContainersIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(instancePath, serializer.serialize(serviceNode));
                log.info("Created instance path: {}", instancePath);
            }
        }
        catch (KeeperException.NodeExistsException e) {
            log.warn("Node already exists.. Race condition?", e);
        }
        catch (Exception e) {
            final String message = String.format(
                    "Could not create node for %s after 60 retries (1 min). " +
                            "This service will not be discoverable. Retry after some time.", service.getServiceName());
            log.error(message, e);
            Exceptions.illegalState(message, e);
        }
    }

    private Optional<List<ServiceNode<T>>> checkForUpdateOnZookeeper(Deserializer<T> deserializer) {
        if (!started.get()) {
            log.warn("Data source is not yet started for service: {}. No nodes will be returned.",
                     service.getServiceName());
            return Optional.empty();
        }
        if (stopped.get()) {
            log.warn("Data source is  stopped already for service: {}. No nodes will be returned.",
                     service.getServiceName());
            return Optional.empty();
        }
        Preconditions.checkNotNull(deserializer, "Deserializer has not been set for node data");
        try {
            final long healthcheckZombieCheckThresholdTime = System.currentTimeMillis() - 60000; //1 Minute
            final String serviceName = service.getServiceName();
            if (!isActive()) {
                log.warn("ZK connection is not active. Ignoring refresh request for service: {}",
                         service.getServiceName());
                return Optional.empty();
            }
            final String parentPath = PathBuilder.servicePath(service);
            log.debug("Looking for node list of [{}]", serviceName);
            List<String> children = curatorFramework.getChildren().forPath(parentPath);
            List<ServiceNode<T>> nodes = Lists.newArrayListWithCapacity(children.size());
            log.debug("Found {} nodes for [{}]", children.size(), serviceName);
            for (String child : children) {
                final String path = String.format("%s/%s", parentPath, child);
                boolean hasChild = null != curatorFramework.checkExists().forPath(path);
                byte[] data = null;
                boolean skipNode = false;
                try {
                    data = hasChild
                           ? curatorFramework.getData().forPath(path)
                           : null;
                }
                catch (KeeperException e) {
                    log.error("Could not get data for node: " + path, e);
                    skipNode = true;
                }
                if (null == data) {
                    log.warn("No data present for node: {} of [{}]", path, serviceName);
                    skipNode = true;
                }
                if (skipNode) {
                    log.debug("Skipping node: {}", path);
                    continue;
                }
                final ServiceNode<T> key = deserializer.deserialize(data);
                if (HealthcheckStatus.healthy == key.getHealthcheckStatus()) {
                    if (key.getLastUpdatedTimeStamp() > healthcheckZombieCheckThresholdTime) {
                        nodes.add(key);
                    }
                    else {
                        log.warn("Zombie node [{}:{}] found for [{}]", key.getHost(), key.getPort(), serviceName);
                    }
                }
            }
            return Optional.of(nodes);
        }
        catch (Exception e) {
            log.error("Error getting service data from zookeeper: ", e);
        }
        return Optional.empty();
    }
}
