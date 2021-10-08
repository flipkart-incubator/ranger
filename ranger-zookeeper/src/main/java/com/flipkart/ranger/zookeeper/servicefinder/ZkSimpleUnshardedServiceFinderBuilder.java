package com.flipkart.ranger.zookeeper.servicefinder;

import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinder;
import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinderBuilder;
import com.flipkart.ranger.core.model.*;
import com.flipkart.ranger.core.signals.Signal;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataDeserializer;
import com.flipkart.ranger.zookeeper.servicefinder.signals.ZkWatcherRegistryUpdateSignal;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.Collections;
import java.util.List;

/**
 *
 */
@Slf4j
public class ZkSimpleUnshardedServiceFinderBuilder<T> extends SimpleUnshardedServiceFinderBuilder<T, ZkSimpleUnshardedServiceFinderBuilder<T>, ZkNodeDataDeserializer<T>, UnshardedCriteria<T>> {
    private CuratorFramework curatorFramework;
    private String connectionString;

    public ZkSimpleUnshardedServiceFinderBuilder<T> withCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
        return this;
    }

    public ZkSimpleUnshardedServiceFinderBuilder<T> withConnectionString(final String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    @Override
    public SimpleUnshardedServiceFinder<T> build() {
        boolean curatorProvided = curatorFramework != null;
        if (!curatorProvided) {
            Preconditions.checkNotNull(connectionString);
            curatorFramework = CuratorFrameworkFactory.builder()
                    .namespace(namespace)
                    .connectString(connectionString)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 100)).build();
            super.withStartSignalHandler(x -> curatorFramework.start());
            super.withStopSignalHandler(x -> curatorFramework.close());
        }
        return buildFinder();
    }

    @Override
    protected NodeDataSource<T, ZkNodeDataDeserializer<T>> dataSource(
            Service service) {
        return new ZkNodeDataSource<>(service, curatorFramework);
    }


    @Override
    protected List<Signal<T>> implementationSpecificRefreshSignals(
            final Service service, final NodeDataSource<T, ZkNodeDataDeserializer<T>> nodeDataSource) {
        if (!disablePushUpdaters) {
            return Collections.singletonList(
                    new ZkWatcherRegistryUpdateSignal<>(service, nodeDataSource, curatorFramework));
        }
        else {
            log.info("Push based signal updater not registered for service: {}", service.getServiceName());
        }
        return Collections.emptyList();
    }}
