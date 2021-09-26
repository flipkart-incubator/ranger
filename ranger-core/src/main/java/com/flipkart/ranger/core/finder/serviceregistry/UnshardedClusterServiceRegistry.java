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

package com.flipkart.ranger.core.finder.serviceregistry;

import com.flipkart.ranger.core.model.UnshardedClusterInfo;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.ServiceRegistry;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UnshardedClusterServiceRegistry extends ServiceRegistry<UnshardedClusterInfo> {
    private AtomicReference<List<ServiceNode<UnshardedClusterInfo>>> nodes
                                        = new AtomicReference<>();

    public UnshardedClusterServiceRegistry(Service service) {
        super(service);
    }

    public List<ServiceNode<UnshardedClusterInfo>> nodes() {
        return nodes.get();
    }

    @Override
    public void updateNodes(List<ServiceNode<UnshardedClusterInfo>> serviceNodes) {
        nodes.set(ImmutableList.copyOf(serviceNodes));
    }
}
