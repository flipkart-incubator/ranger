package com.flipkart.ranger.zookeeper.zk;

import com.flipkart.ranger.core.model.NodeDataSource;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.util.FinderUtils;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataDeserializer;
import com.flipkart.ranger.zookeeper.util.PathBuilder;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;

import java.util.List;
import java.util.Optional;

/**
 *
 */
@Slf4j
public class ZkNodeDataSource<T, D extends ZkNodeDataDeserializer<T>> extends ZkNodeDataStoreConnector<T> implements NodeDataSource<T, D> {

    public ZkNodeDataSource(
            Service service,
            CuratorFramework curatorFramework) {
        super(service, curatorFramework);
    }

    @Override
    public Optional<List<ServiceNode<T>>> refresh(D deserializer) {
        return checkForUpdateOnZookeeper(deserializer);
    }

    private Optional<List<ServiceNode<T>>> checkForUpdateOnZookeeper(D deserializer) {
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
            final long healthcheckZombieCheckThresholdTime = healthcheckZombieCheckThresholdTime(service); //1 Minute
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
                final ServiceNode<T> node = deserializer.deserialize(data);
                if(FinderUtils.isValidNode(service, healthcheckZombieCheckThresholdTime, node)) {
                    nodes.add(node);
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
