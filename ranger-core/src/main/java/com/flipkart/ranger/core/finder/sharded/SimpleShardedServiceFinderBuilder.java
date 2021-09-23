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

package com.flipkart.ranger.core.finder.sharded;

import com.flipkart.ranger.core.finder.BaseServiceFinderBuilder;
import com.flipkart.ranger.core.model.Deserializer;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNodeSelector;
import com.flipkart.ranger.core.model.ShardSelector;

public abstract class SimpleShardedServiceFinderBuilder<T, B extends SimpleShardedServiceFinderBuilder<T,B, D>, D extends Deserializer<T>>
        extends BaseServiceFinderBuilder<T, MapBasedServiceRegistry<T>, SimpleShardedServiceFinder<T>, B, D> {

    @Override
    protected SimpleShardedServiceFinder<T> buildFinder(
            Service service,
            ShardSelector<T, MapBasedServiceRegistry<T>> shardSelector,
            ServiceNodeSelector<T> nodeSelector) {
        if (null == shardSelector) {
            shardSelector = new MatchingShardSelector<>();
        }
        final MapBasedServiceRegistry<T> serviceRegistry = new MapBasedServiceRegistry<>(service);
        return new SimpleShardedServiceFinder<>(serviceRegistry, shardSelector, nodeSelector);
    }
}
