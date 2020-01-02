package com.flipkart.ranger.zookeeper.zk;

import com.flipkart.ranger.core.finderhub.ServiceDataSource;
import com.flipkart.ranger.core.model.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
public class ZkServiceDataSource implements ServiceDataSource {

    private final String namespace;
    private final CuratorFramework curatorFramework;

    public ZkServiceDataSource(String namespace, CuratorFramework curatorFramework) {
        this.namespace = namespace;
        this.curatorFramework = curatorFramework;
    }

    @Override
    public Collection<Service> services() throws Exception {
        final List<String> children = curatorFramework.getChildren()
                .forPath("/");
        return children.stream()
                .map(child -> new Service(namespace, child))
                .collect(Collectors.toSet());
    }

    @Override
    public void start() {
        try {
            curatorFramework.blockUntilConnected();
        }
        catch (InterruptedException e) {
            log.error("Curator block interrupted", e);
        }
        log.info("Service data source started. Curator state is: {}", curatorFramework.getState().name());
    }

    @Override
    public void stop() {
        log.info("Service data source stopped");
    }
}
