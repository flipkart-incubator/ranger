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

import com.flipkart.ranger.finder.BaseServiceFinderBuilder;
import com.flipkart.ranger.finder.Service;
import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.ServiceNodeSelector;
import com.flipkart.ranger.model.ShardSelector;

public class UnshardedFinderBuilder extends BaseServiceFinderBuilder<UnshardedClusterInfo, UnshardedClusterServiceRegistry, UnshardedClusterFinder> {
    @Override
    protected UnshardedClusterFinder buildFinder(Service service,
                                                 Deserializer<UnshardedClusterInfo> deserializer,
                                                 ShardSelector<UnshardedClusterInfo,
                                                 UnshardedClusterServiceRegistry> shardSelector,
                                                 ServiceNodeSelector<UnshardedClusterInfo> nodeSelector,
                                                 int healthcheckRefreshTimeMillis,
                                                 int minNodesAvailablePercentage) {
        UnshardedClusterServiceRegistry unshardedClusterServiceRegistry
                = new UnshardedClusterServiceRegistry(service, deserializer, healthcheckRefreshTimeMillis, minNodesAvailablePercentage);
        return new UnshardedClusterFinder(unshardedClusterServiceRegistry, new NoOpShardSelector(), nodeSelector);
    }
}
