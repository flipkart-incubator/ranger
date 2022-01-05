/*
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
package io.appform.ranger.core.finder.serviceregistry;

import io.appform.ranger.core.model.Service;
import io.appform.ranger.core.model.ServiceNode;
import io.appform.ranger.core.model.ServiceRegistry;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@EqualsAndHashCode(callSuper = true)
public class MapBasedServiceRegistry<T> extends ServiceRegistry<T> {
    private final AtomicReference<ListMultimap<T, ServiceNode<T>>> nodes = new AtomicReference<>();

    public MapBasedServiceRegistry(Service service) {
        super(service);
    }

    public ListMultimap<T, ServiceNode<T>> nodes() {
        val nodeList = nodes.get();
        return null == nodeList ? ImmutableListMultimap.of() : nodeList;
    }

    @Override
    public List<ServiceNode<T>> nodeList() {
        val nodeList = nodes.get();
        return null == nodeList ? ImmutableList.of() : new ArrayList<>(nodeList.values());
    }

    @Override
    public void updateNodes(List<ServiceNode<T>> nodes) {
        ListMultimap<T, ServiceNode<T>> serviceNodes = ArrayListMultimap.create();
        nodes.forEach(serviceNode -> serviceNodes.put(serviceNode.getNodeData(), serviceNode));
        this.nodes.set(ImmutableListMultimap.copyOf(serviceNodes));
    }
}
