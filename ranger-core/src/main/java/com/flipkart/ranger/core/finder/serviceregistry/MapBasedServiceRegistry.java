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

package com.flipkart.ranger.core.finder.serviceregistry;

import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.ServiceRegistry;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MapBasedServiceRegistry<T> extends ServiceRegistry<T> {
    private AtomicReference<ListMultimap<T, ServiceNode<T>>> nodes = new AtomicReference<>();

    public MapBasedServiceRegistry(Service service) {
        super(service);
    }

    public ListMultimap<T, ServiceNode<T>> nodes() {
        final ListMultimap<T, ServiceNode<T>> nodeList = nodes.get();
        return null == nodeList ? ImmutableListMultimap.of() : nodeList;
    }

    @Override
    public List<ServiceNode<T>> nodeList() {
        final ListMultimap<T, ServiceNode<T>> nodeList = nodes.get();
        return null == nodeList ? Collections.emptyList() : new ArrayList<>(nodeList.values());
    }

    @Override
    public void updateNodes(List<ServiceNode<T>> nodes) {
        ListMultimap<T, ServiceNode<T>> serviceNodes = ArrayListMultimap.create();
        for (ServiceNode<T> serviceNode : nodes) {
            serviceNodes.put(serviceNode.getNodeData(), serviceNode);
        }
        this.nodes.set(ImmutableListMultimap.copyOf(serviceNodes));
    }

}