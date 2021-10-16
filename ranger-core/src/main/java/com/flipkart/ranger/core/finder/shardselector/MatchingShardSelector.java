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

package com.flipkart.ranger.core.finder.shardselector;

import com.flipkart.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.ShardSelector;

import java.util.List;

public class MatchingShardSelector<T> implements ShardSelector<T, MapBasedServiceRegistry<T>> {


    @Override
    public List<ServiceNode<T>> nodes(T criteria, MapBasedServiceRegistry<T> serviceRegistry) {
        if(criteria == null){
            return serviceRegistry.nodeList();
        }

        return serviceRegistry.nodes().get(criteria);
    }
}
