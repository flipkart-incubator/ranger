/*
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
package com.flipkart.ranger.client.stubs;

import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinder;
import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinderBuilder;
import com.flipkart.ranger.core.model.*;
import com.google.common.collect.Lists;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

@Builder
public class TestSimpleUnshardedServiceFinder <T, C extends Criteria<T>>
        extends SimpleUnshardedServiceFinderBuilder<TestShardInfo, TestSimpleUnshardedServiceFinder<T, C>, Deserializer<TestShardInfo>, Criteria<TestShardInfo>> {

    @Override
    public SimpleUnshardedServiceFinder<TestShardInfo, Criteria<TestShardInfo>> build() {
        return buildFinder();
    }

    @Override
    protected NodeDataSource<TestShardInfo, Deserializer<TestShardInfo>> dataSource(Service service) {
        return new NodeDataSource<TestShardInfo, Deserializer<TestShardInfo>>() {
            @Override
            public Optional<List<ServiceNode<TestShardInfo>>> refresh(Deserializer<TestShardInfo> deserializer) {
                return Optional.of(
                        Lists.newArrayList(
                                new ServiceNode<>("localhost", 9200, TestShardInfo.builder().shardId(1).build())
                        )
                );
            }

            @Override
            public void start() {

            }

            @Override
            public void ensureConnected() {

            }

            @Override
            public void stop() {

            }

            @Override
            public boolean isActive() {
                return true;
            }
        };
    }
}

