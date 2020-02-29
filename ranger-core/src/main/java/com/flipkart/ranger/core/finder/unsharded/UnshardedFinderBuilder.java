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

package com.flipkart.ranger.core.finder.unsharded;

import com.flipkart.ranger.core.model.Deserializer;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ShardSelector;
import com.flipkart.ranger.core.finder.BaseServiceFinderBuilder;
import com.flipkart.ranger.core.model.ServiceNodeSelector;

public abstract class UnshardedFinderBuilder<B extends UnshardedFinderBuilder<B, D>, D extends Deserializer<UnshardedClusterInfo>>
        extends BaseServiceFinderBuilder<UnshardedClusterInfo, UnshardedClusterServiceRegistry, UnshardedClusterFinder, B, D> {

    @Override
    protected UnshardedClusterFinder buildFinder(
            Service service,
            ShardSelector<UnshardedClusterInfo, UnshardedClusterServiceRegistry> shardSelector,
            ServiceNodeSelector<UnshardedClusterInfo> nodeSelector) {
        final UnshardedClusterServiceRegistry unshardedClusterServiceRegistry
                = new UnshardedClusterServiceRegistry(service);
        return new UnshardedClusterFinder(unshardedClusterServiceRegistry, new NoOpShardSelector(), nodeSelector);
    }
}
