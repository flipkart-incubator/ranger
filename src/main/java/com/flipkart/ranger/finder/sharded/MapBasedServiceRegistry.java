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

package com.flipkart.ranger.finder.sharded;

import com.flipkart.ranger.finder.AbstractZookeeperServiceRegistry;
import com.flipkart.ranger.finder.Service;
import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.ServiceNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MapBasedServiceRegistry<T> extends AbstractZookeeperServiceRegistry<T> {
    private AtomicReference<ListMultimap<T,ServiceNode<T>>> nodes = new AtomicReference<ListMultimap<T, ServiceNode<T>>>();

    public MapBasedServiceRegistry(Service service, Deserializer<T> deserializer, int refreshInterval, int minNodesAvailablePercentage) {
        super(service, deserializer, refreshInterval, minNodesAvailablePercentage);
    }

    public ListMultimap<T, ServiceNode<T>> nodes() {
        return nodes.get();
    }

    @Override
    public void nodes(List<ServiceNode<T>> nodes) {
        ListMultimap<T, ServiceNode<T>> serviceNodes = ArrayListMultimap.create();
        for(ServiceNode<T> serviceNode : nodes) {
            serviceNodes.put(serviceNode.getNodeData(), serviceNode);
        }
        this.nodes.set(ImmutableListMultimap.copyOf(serviceNodes));
    }

}
