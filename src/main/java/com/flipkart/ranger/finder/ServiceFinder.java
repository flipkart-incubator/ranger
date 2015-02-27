/**
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
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

import java.util.List;

public class ServiceFinder<T, ServiceRegistryType extends ServiceRegistry<T>> {
    private final ServiceRegistryType serviceRegistry;
    private final ShardSelector<T, ServiceRegistryType> shardSelector;
    private final ServiceNodeSelector<T> nodeSelector;

    public ServiceFinder(ServiceRegistryType serviceRegistry, ShardSelector<T, ServiceRegistryType> shardSelector, ServiceNodeSelector<T> nodeSelector) {
        this.serviceRegistry = serviceRegistry;
        this.shardSelector = shardSelector;
        this.nodeSelector = nodeSelector;
    }

    public ServiceNode<T> get(T criteria) {
        List<ServiceNode<T>> nodes = shardSelector.nodes(criteria, serviceRegistry);
        if(null == nodes || nodes.isEmpty()) {
            return null;
        }
        return nodeSelector.select(nodes);
    }

    public List<ServiceNode<T>> getAll(T criteria) {
        return shardSelector.nodes(criteria, serviceRegistry);
    }

    public void start() throws Exception {
        serviceRegistry.start();
    }

    public void stop() throws Exception {
        serviceRegistry.stop();
    }
}
