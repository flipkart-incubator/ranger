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
package io.appform.ranger.zookeeper.servicefinder.signals;

import io.appform.ranger.core.model.NodeDataStoreConnector;
import io.appform.ranger.core.model.Service;
import io.appform.ranger.core.signals.Signal;
import io.appform.ranger.zookeeper.util.PathBuilder;
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
    private final NodeDataStoreConnector<T> dataSource;
    private final CuratorFramework curatorFramework;

    public ZkWatcherRegistryUpdateSignal(
            Service service,
            NodeDataStoreConnector<T> dataSource,
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
