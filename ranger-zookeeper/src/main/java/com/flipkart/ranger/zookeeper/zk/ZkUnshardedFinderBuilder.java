package com.flipkart.ranger.zookeeper.zk;

import com.flipkart.ranger.core.model.NodeDataSource;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.finder.unsharded.UnshardedClusterFinder;
import com.flipkart.ranger.core.finder.unsharded.UnshardedClusterInfo;
import com.flipkart.ranger.core.finder.unsharded.UnshardedFinderBuilder;
import com.flipkart.ranger.core.model.Deserializer;
import com.flipkart.ranger.core.model.Serializer;
import com.flipkart.ranger.core.signals.Signal;
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
public class ZkUnshardedFinderBuilder extends UnshardedFinderBuilder<ZkUnshardedFinderBuilder> {
    private CuratorFramework curatorFramework;
    private String connectionString;

    public ZkUnshardedFinderBuilder withCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
        return this;
    }

    public ZkUnshardedFinderBuilder withConnectionString(final String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    @Override
    public UnshardedClusterFinder build() {
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
    protected NodeDataSource<UnshardedClusterInfo> dataSource(
            Service service,
            Serializer<UnshardedClusterInfo> serializer,
            Deserializer<UnshardedClusterInfo> deserializer) {
        return new ZkNodeDataSource<>(service, null, deserializer, curatorFramework);
    }


    @Override
    protected List<Signal<UnshardedClusterInfo>> implementationSpecificRefreshSignals(
            final Service service, final NodeDataSource<UnshardedClusterInfo> nodeDataSource) {
        if (!disablePushUpdaters) {
            return Collections.singletonList(
                    new ZkWatcherRegistryUpdateSignal<>(service, nodeDataSource, curatorFramework));
        }
        else {
            log.info("Push based signal updater not registered for service: {}", service.getServiceName());
        }
        return Collections.emptyList();
    }}
