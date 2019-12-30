package com.flipkart.ranger.zookeeper.zk;

import com.flipkart.ranger.core.model.NodeDataSource;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.zookeeper.util.PathBuilder;
import com.flipkart.ranger.core.signals.Signal;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.Watcher;

import java.util.Collections;

/**
 *
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ZkWatcherRegistryUpdateSignal<T> extends Signal<T> {
    private final Service service;
    private final NodeDataSource<T> dataSource;
    private final CuratorFramework curatorFramework;

    public ZkWatcherRegistryUpdateSignal(
            Service service,
            NodeDataSource<T> dataSource,
            CuratorFramework curatorFramework) {
        super(() -> null, Collections.emptyList());
        this.service = service;
        this.dataSource = dataSource;
        this.curatorFramework = curatorFramework;
    }

    @Override
    public void start() {
        dataSource.ensureConnected();
        log.info("Node data source is connected, Initializing watchers for service: {}",
                 service.getServiceName());
        try {
            curatorFramework.getChildren()
                    .usingWatcher((CuratorWatcher) event -> {
                        if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                            onSignalReceived();
                        }
                    })
                    .forPath(PathBuilder.servicePath(service)); //Start watcher on service node
        } catch (Exception e) {
            log.error("Could not setup ZK watchers for service: " + service.getServiceName(), e);
        }
        log.info("Started polling zookeeper for changes for service: {}", service.getServiceName());
    }

    @Override
    public void stop() {
        //Nothing to stop here
    }
}
