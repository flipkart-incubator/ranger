/**
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

package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.model.ServiceNodeSelector;
import com.flipkart.ranger.model.ServiceRegistry;
import com.flipkart.ranger.model.ShardSelector;
import com.flipkart.ranger.util.Exceptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public abstract class ServiceFinder<T, ServiceRegistryType extends ServiceRegistry<T>> {
    @Getter
    private final ServiceRegistryType serviceRegistry;
    private final ShardSelector<T, ServiceRegistryType> shardSelector;
    private final ServiceNodeSelector<T> nodeSelector;
    private final AtomicReference<ServiceRegistryUpdater<T>> registryUpdater = new AtomicReference<>();

    protected ServiceFinder(
            ServiceRegistryType serviceRegistry,
            ShardSelector<T, ServiceRegistryType> shardSelector,
            ServiceNodeSelector<T> nodeSelector) {
        this.serviceRegistry = serviceRegistry;
        this.shardSelector = shardSelector;
        this.nodeSelector = nodeSelector;
    }

    public ServiceNode<T> get(T criteria) {
        List<ServiceNode<T>> nodes = shardSelector.nodes(criteria, serviceRegistry);
        if (null == nodes || nodes.isEmpty()) {
            return null;
        }
        return nodeSelector.select(nodes);
    }

    public List<ServiceNode<T>> getAll(T criteria) {
        return shardSelector.nodes(criteria, serviceRegistry);
    }

    public void registerUpdater(ServiceRegistryUpdater<T> registryUpdater) {
        if (this.registryUpdater.compareAndSet(null, registryUpdater)) {
            log.info("Registry updater set in finder for service: {}", serviceRegistry.getService().getServiceName());
        }
        else {
            log.warn("Repeat Registry update request in finder for service: {}",
                     serviceRegistry.getService().getServiceName());
        }
    }

    public void start() {
        final ServiceRegistryUpdater<T> updater = registryUpdater.get();
        if (null != updater) {
            updater.start();
        }
        else {
            Exceptions.illegalState("Start called in finder, but no updater registered for service: "
                                            + serviceRegistry.getService().getServiceName());
        }
    }

    public void stop() {
        final ServiceRegistryUpdater<T> updater = registryUpdater.get();
        if (null != updater) {
            updater.stop();
        }
        else {
            log.warn("Stop called for finder, even before registry updater is ser for service: "
                             + serviceRegistry.getService().getServiceName());
        }
    }
}
