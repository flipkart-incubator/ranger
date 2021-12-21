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
package com.flipkart.ranger.zookeeper.servicefinder;

import com.flipkart.ranger.core.model.NodeDataSource;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.util.FinderUtils;
import com.flipkart.ranger.zookeeper.common.ZkNodeDataStoreConnector;
import com.flipkart.ranger.zookeeper.common.ZkStoreType;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataDeserializer;
import com.flipkart.ranger.zookeeper.util.PathBuilder;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;

import java.util.Collections;
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
        super(service, curatorFramework, ZkStoreType.SOURCE);
    }

    @Override
    public List<ServiceNode<T>> refresh(D deserializer) {
        return checkForUpdateOnZookeeper(deserializer);
    }

    private List<ServiceNode<T>> checkForUpdateOnZookeeper(D deserializer) {
        if (!isStarted()) {
            log.warn("Data source is not yet started for service: {}. No nodes will be returned.",
                     service.getServiceName());
            return Collections.emptyList();
        }
        if (isStopped()) {
            log.warn("Data source is  stopped already for service: {}. No nodes will be returned.",
                     service.getServiceName());
            return Collections.emptyList();
        }
        Preconditions.checkNotNull(deserializer, "Deserializer has not been set for node data");
        try {
            val healthcheckZombieCheckThresholdTime = healthcheckZombieCheckThresholdTime(service); //1 Minute
            val serviceName = service.getServiceName();
            if (!isActive()) {
                log.warn("ZK connection is not active. Ignoring refresh request for service: {}",
                         service.getServiceName());
                return Collections.emptyList();
            }
            val parentPath = PathBuilder.servicePath(service);
            log.debug("Looking for node list of [{}]", serviceName);
            val children = curatorFramework.getChildren().forPath(parentPath);
            List<ServiceNode<T>> nodes = Lists.newArrayListWithCapacity(children.size());
            log.debug("Found {} nodes for [{}]", children.size(), serviceName);
            for (val child : children) {
                byte[] data = readChild(parentPath, child).orElse(null);
                if (data == null || data.length <= 0) {
                    continue;
                }
                val node = deserializer.deserialize(data);
                if(FinderUtils.isValidNode(service, healthcheckZombieCheckThresholdTime, node)) {
                    nodes.add(node);
                }
            }
            return nodes;
        }
        catch (Exception e) {
            log.error("Error getting service data from zookeeper: ", e);
        }
        return Collections.emptyList();
    }

    private Optional<byte[]> readChild(String parentPath, String child) throws Exception {
        final String path = String.format("%s/%s", parentPath, child);
        try {
            return Optional.ofNullable(curatorFramework.getData().forPath(path));
        }
        catch (KeeperException.NoNodeException e) {
            log.warn("Node not found for path {}", path);
            return Optional.empty();
        }
        catch (KeeperException e) {
            log.error("Could not get data for node: " + path, e);
            return Optional.empty();
        }
    }

}
