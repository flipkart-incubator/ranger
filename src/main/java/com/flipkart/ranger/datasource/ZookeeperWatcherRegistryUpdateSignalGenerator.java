package com.flipkart.ranger.datasource;

import com.flipkart.ranger.finder.Service;
import com.flipkart.ranger.model.PathBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.Watcher;

/**
 *
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ZookeeperWatcherRegistryUpdateSignalGenerator<T> extends RegistryUpdateSignalGenerator<T> {
    private final CuratorFramework curatorFramework;

    public ZookeeperWatcherRegistryUpdateSignalGenerator(
            Service service,
            NodeDataSource<T> dataSource,
            CuratorFramework curatorFramework) {
        super(service, dataSource);
        this.curatorFramework = curatorFramework;
    }

    @Override
    public void start() {
        final Service service = getService();
        getDataSource().ensureConnected();
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
    public void shutdown() {

    }
}
