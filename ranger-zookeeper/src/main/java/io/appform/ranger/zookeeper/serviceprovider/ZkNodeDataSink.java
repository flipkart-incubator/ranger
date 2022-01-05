/*
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.appform.ranger.zookeeper.serviceprovider;

import io.appform.ranger.core.model.NodeDataSink;
import io.appform.ranger.core.model.Service;
import io.appform.ranger.core.model.ServiceNode;
import io.appform.ranger.core.util.Exceptions;
import io.appform.ranger.zookeeper.common.ZkNodeDataStoreConnector;
import io.appform.ranger.zookeeper.common.ZkStoreType;
import io.appform.ranger.zookeeper.serde.ZkNodeDataSerializer;
import io.appform.ranger.zookeeper.util.PathBuilder;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
        super(service, curatorFramework, ZkStoreType.SINK);
    }

    @Override
    public void updateState(S serializer, ServiceNode<T> serviceNode) {
        if (isStopped()) {
            log.warn("Node has been stopped already for service: {}. No update will be possible.",
                     service.getServiceName());
            return;
        }
        Preconditions.checkNotNull(serializer, "Serializer has not been set for node data");
        val path = PathBuilder.instancePath(service, serviceNode);
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
        val instancePath = PathBuilder.instancePath(service, serviceNode);
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
            val message = String.format(
                    "Could not create node for %s after 60 retries (1 min). " +
                            "This service will not be discoverable. Retry after some time.", service.getServiceName());
            log.error(message, e);
            Exceptions.illegalState(message, e);
        }
    }


}
