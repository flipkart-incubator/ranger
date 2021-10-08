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

package com.flipkart.ranger.zookeeper;

import com.flipkart.ranger.zookeeper.servicefinder.ZkSimpleShardedServiceFinderBuilder;
import com.flipkart.ranger.zookeeper.servicefinder.ZkSimpleUnshardedServiceFinderBuilder;

public class ServiceFinderBuilders {
    
    private ServiceFinderBuilders() {
        throw new InstantiationError("Must not instantiate this class");
    }
    
    public static <T> ZkSimpleShardedServiceFinderBuilder<T> shardedFinderBuilder() {
        return new ZkSimpleShardedServiceFinderBuilder<>();
    }

    public static <T> ZkSimpleUnshardedServiceFinderBuilder<T> unshardedFinderBuilder() {
        return new ZkSimpleUnshardedServiceFinderBuilder<>();
    }
}
