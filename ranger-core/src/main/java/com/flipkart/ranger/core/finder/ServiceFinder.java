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

package com.flipkart.ranger.core.finder;

import com.flipkart.ranger.core.model.*;
import com.flipkart.ranger.core.signals.ExternalTriggeredSignal;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
public abstract class ServiceFinder<T, ServiceRegistryType extends ServiceRegistry<T>, U extends Criteria<T, ServiceRegistryType>> {
    @Getter
    private final ServiceRegistryType serviceRegistry;
    private final ShardSelector<T, ServiceRegistryType, U> shardSelector;
    private final ServiceNodeSelector<T> nodeSelector;
    @Getter
    private final ExternalTriggeredSignal<Void> startSignal = new ExternalTriggeredSignal<>(() -> null, Collections.emptyList());
    @Getter
    private final ExternalTriggeredSignal<Void> stopSignal = new ExternalTriggeredSignal<>(() -> null, Collections.emptyList());

    protected ServiceFinder(
            ServiceRegistryType serviceRegistry,
            ShardSelector<T, ServiceRegistryType, U> shardSelector,
            ServiceNodeSelector<T> nodeSelector) {
        this.serviceRegistry = serviceRegistry;
        this.shardSelector = shardSelector;
        this.nodeSelector = nodeSelector;
    }

    public ServiceNode<T> get(U criteria) {
        List<ServiceNode<T>> nodes = shardSelector.nodes(criteria, serviceRegistry);
        if (null == nodes || nodes.isEmpty()) {
            return null;
        }
        return nodeSelector.select(nodes);
    }

    public List<ServiceNode<T>> getAll(U criteria) {
        return shardSelector.nodes(criteria, serviceRegistry);
    }

    public void start() {
        startSignal.trigger();
    }

    public void stop() {
        stopSignal.trigger();
    }
}
