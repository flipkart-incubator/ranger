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
package io.appform.ranger.client.stubs;

import io.appform.ranger.core.finder.SimpleUnshardedServiceFinder;
import io.appform.ranger.core.finder.SimpleUnshardedServiceFinderBuilder;
import io.appform.ranger.core.model.Deserializer;
import io.appform.ranger.core.model.NodeDataSource;
import io.appform.ranger.core.model.Service;
import io.appform.ranger.core.model.ServiceNode;
import io.appform.ranger.core.units.TestNodeData;
import lombok.Builder;

import java.util.Collections;
import java.util.List;

@Builder
public class TestSimpleUnshardedServiceFinder <T>
        extends SimpleUnshardedServiceFinderBuilder<TestNodeData, TestSimpleUnshardedServiceFinder<T>, Deserializer<TestNodeData>> {

    @Override
    public SimpleUnshardedServiceFinder<TestNodeData> build() {
        return buildFinder();
    }

    @Override
    protected NodeDataSource<TestNodeData, Deserializer<TestNodeData>> dataSource(Service service) {
        return new TestDataSource();
    }

    static class TestDataSource implements NodeDataSource<TestNodeData, Deserializer<TestNodeData>>{

        @Override
        public List<ServiceNode<TestNodeData>> refresh(Deserializer<TestNodeData> deserializer) {
            return Collections.singletonList(
                    ServiceNode.<TestNodeData>builder()
                            .host("localhost")
                            .port(9200)
                            .nodeData(TestNodeData.builder().shardId(1).build())
                            .build()
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
    }
}

