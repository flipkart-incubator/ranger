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

import com.flipkart.ranger.finder.BaseServiceFinderBuilder;
import com.flipkart.ranger.finder.Service;
import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.ServiceNodeSelector;
import com.flipkart.ranger.model.ShardSelector;

public class SimpleShardedServiceFinderBuilder<T> extends BaseServiceFinderBuilder<T, MapBasedServiceRegistry<T>, SimpleShardedServiceFinder<T>> {
    @Override
    protected SimpleShardedServiceFinder<T> buildFinder(Service service,
                                                        Deserializer<T> deserializer,
                                                        ShardSelector<T, MapBasedServiceRegistry<T>> shardSelector,
                                                        ServiceNodeSelector<T> nodeSelector,
                                                        int healthcheckRefreshTimeMillis,
                                                        int minNodesAvailablePercentage) {
        if(null == shardSelector) {
            shardSelector = new MatchingShardSelector<T>();
        }
        MapBasedServiceRegistry<T> serviceRegistry = new MapBasedServiceRegistry<T>(service, deserializer, healthcheckRefreshTimeMillis, minNodesAvailablePercentage);
        return new SimpleShardedServiceFinder<T>(serviceRegistry, shardSelector, nodeSelector);
    }
}
