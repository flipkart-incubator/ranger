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
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@EqualsAndHashCode(callSuper = true)
public class ListBasedServiceRegistry<T> extends ServiceRegistry<T> {
    private final AtomicReference<List<ServiceNode<T>>> nodes
                                        = new AtomicReference<>();

    public ListBasedServiceRegistry(Service service) {
        super(service);
    }

    public List<ServiceNode<T>> nodeList() {
        val nodeList = this.nodes.get();
        return null == nodeList ? Collections.emptyList() : nodeList;
    }

    @Override
    public void updateNodes(List<ServiceNode<T>> serviceNodes) {
        nodes.set(ImmutableList.copyOf(serviceNodes));
    }
}
