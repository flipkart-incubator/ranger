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

import com.flipkart.ranger.finder.AbstractServiceRegistryUpdater;
import com.flipkart.ranger.finder.ServiceFinder;
import com.flipkart.ranger.model.ServiceNodeSelector;
import com.flipkart.ranger.model.ShardSelector;

public class SimpleShardedServiceFinder<T> extends ServiceFinder<T, MapBasedServiceRegistry<T>> {
    public SimpleShardedServiceFinder(MapBasedServiceRegistry<T> serviceRegistry, AbstractServiceRegistryUpdater<T> updater,
                                      ShardSelector<T, MapBasedServiceRegistry<T>> shardSelector,
                                      ServiceNodeSelector<T> nodeSelector, int healthcheckRefreshTimeMillis) {
        super(serviceRegistry, updater, shardSelector, nodeSelector, healthcheckRefreshTimeMillis);
    }
}
