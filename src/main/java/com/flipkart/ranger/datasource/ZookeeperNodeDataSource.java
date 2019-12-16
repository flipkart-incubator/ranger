package com.flipkart.ranger.datasource;

import com.flipkart.ranger.finder.Service;
import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.PathBuilder;
import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.util.Exceptions;
import com.github.rholder.retry.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.zookeeper.KeeperException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
@Slf4j
public class ZookeeperNodeDataSource<T> implements NodeDataSource<T> {

    private final Service service;
    private final Deserializer<T> deserializer;
    private final CuratorFramework curatorFramework;
    private final Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
            .retryIfException(e -> IllegalStateException.class.isAssignableFrom(e.getClass()))
            .retryIfResult(aBoolean -> false)
            .withAttemptTimeLimiter(AttemptTimeLimiters.noTimeLimit())
            .withWaitStrategy(WaitStrategies.exponentialWait(10, TimeUnit.SECONDS))
            .withBlockStrategy(BlockStrategies.threadSleepStrategy())
            .withRetryListener(new RetryListener() {
                @Override
                public <V> void onRetry(Attempt<V> attempt) {
                    log.debug("Retrying with attempt: {}", attempt);
                }
            })
            .build();
    private final AtomicBoolean started = new AtomicBoolean(false);

    public ZookeeperNodeDataSource(
            final Service service,
            final Deserializer<T> deserializer,
            final CuratorFramework curatorFramework) {
        this.service = service;
        this.deserializer = deserializer;
        this.curatorFramework = curatorFramework;
    }

    @Override
    public void start() {
        final String path = PathBuilder.path(service);
        try {
            curatorFramework.blockUntilConnected();
            log.info("Connected to zookeeper cluster for {}", service.getServiceName());
            curatorFramework
                    .create()
                    .creatingParentContainersIfNeeded()
                    .forPath(path);
        }
        catch (KeeperException e) {
            if(e.code() == KeeperException.Code.NODEEXISTS) {
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
            retryer.call(this::isActive);
        }
        catch (Exception e) {
            Exceptions.illegalState("Could not get zk connection", e);
        }
    }

    @Override
    public void stop() {
        if (!started.get()) {
            log.warn("Shutdown called for service: {}, but data source is not started.", service.getServiceName());
        }
        log.info("Shutting down data source for service: {}.  (It's a no-op.)",
                 service.getServiceName());
    }

    @Override
    public Optional<List<ServiceNode<T>>> refresh() {
        return checkForUpdateOnZookeeper();
    }

    @Override
    public boolean isActive() {
        return curatorFramework != null
                && (curatorFramework.getState() == CuratorFrameworkState.STARTED);
    }

    private Optional<List<ServiceNode<T>>> checkForUpdateOnZookeeper() {
        try {
            final long healthcheckZombieCheckThresholdTime = System.currentTimeMillis() - 60000; //1 Minute
            final String serviceName = service.getServiceName();
            if (!isActive()) {
                log.warn("ZK connection is not active. Ignoring refresh request for service: {}",
                          service.getServiceName());
                return Optional.empty();
            }
            final String parentPath = PathBuilder.path(service);
            log.debug("Looking for node list of [{}]", serviceName);
            List<String> children = curatorFramework.getChildren().forPath(parentPath);
            List<ServiceNode<T>> nodes = Lists.newArrayListWithCapacity(children.size());
            log.debug("Found {} nodes for [{}]", children.size(), serviceName);
            for (String child : children) {
                final String path = String.format("%s/%s", parentPath, child);
                boolean hasChild = null != curatorFramework.checkExists().forPath(path);
                final byte[] data;
                try {
                 data = hasChild
                   ? curatorFramework.getData().forPath(path)
                   : null;

                } catch (KeeperException e) {
                    log.info("Could not get data for node: " + path, e);
                    continue;
                }
                if (null == data) {
                    log.warn("No data present for node: {} of [{}]", path, serviceName);
                    continue;
                }
                ServiceNode<T> key = deserializer.deserialize(data);
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
