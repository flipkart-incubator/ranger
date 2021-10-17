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

package com.flipkart.ranger.core.finder;

import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import com.flipkart.ranger.core.finder.shardselector.ListShardSelector;
import com.flipkart.ranger.core.model.*;

public abstract class SimpleUnshardedServiceFinderBuilder<T, B extends SimpleUnshardedServiceFinderBuilder<T, B, D, C>, D extends Deserializer<T>, C extends Criteria<T>>
        extends BaseServiceFinderBuilder<T, ListBasedServiceRegistry<T>, SimpleUnshardedServiceFinder<T, C>, B, D, C> {

    @Override
    protected SimpleUnshardedServiceFinder<T, C> buildFinder(
            Service service,
            ShardSelector<T, C, ListBasedServiceRegistry<T>> shardSelector,
            ServiceNodeSelector<T> nodeSelector
    ) {
        if (null == shardSelector) {
            shardSelector = new ListShardSelector<>();
        }
        final ListBasedServiceRegistry<T> serviceRegistry = new ListBasedServiceRegistry<>(service);
        return new SimpleUnshardedServiceFinder<>(serviceRegistry, shardSelector, nodeSelector);
    }
}