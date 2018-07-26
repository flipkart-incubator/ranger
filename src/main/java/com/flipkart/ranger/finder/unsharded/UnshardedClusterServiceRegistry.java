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

package com.flipkart.ranger.finder.unsharded;

import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.model.ServiceRegistry;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UnshardedClusterServiceRegistry implements ServiceRegistry<UnshardedClusterInfo> {
    private AtomicReference<List<ServiceNode<UnshardedClusterInfo>>> nodes
                                        = new AtomicReference<List<ServiceNode<UnshardedClusterInfo>>>();

    public List<ServiceNode<UnshardedClusterInfo>> nodes() {
        return nodes.get();
    }

    public void nodes(List<ServiceNode<UnshardedClusterInfo>> serviceNodes) {
        nodes.set(ImmutableList.copyOf(serviceNodes));
    }
}
