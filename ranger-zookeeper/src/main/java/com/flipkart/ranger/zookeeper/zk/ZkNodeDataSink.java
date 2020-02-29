package com.flipkart.ranger.zookeeper.zk;

import com.flipkart.ranger.core.model.NodeDataSink;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.util.Exceptions;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataSerializer;
import com.flipkart.ranger.zookeeper.util.PathBuilder;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 *
 */
@Slf4j
public class ZkNodeDataSink<T, S extends ZkNodeDataSerializer<T>> extends ZkNodeDataStoreConnector<T> implements NodeDataSink<T,S> {
    public ZkNodeDataSink(
            Service service,
            CuratorFramework curatorFramework) {
        super(service, curatorFramework);
    }

    @Override
    public void updateState(S serializer, ServiceNode<T> serviceNode) {
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
            S serializer) {
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


}
