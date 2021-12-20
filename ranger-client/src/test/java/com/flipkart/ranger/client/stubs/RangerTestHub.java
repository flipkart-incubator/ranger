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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.client.AbstractRangerHubClient;
import com.flipkart.ranger.client.utils.RangerHubTestUtils;
import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import com.flipkart.ranger.core.finderhub.*;
import com.flipkart.ranger.core.units.TestNodeData;
import com.google.common.collect.Sets;
import lombok.Builder;

import java.util.function.Predicate;

public class RangerTestHub extends AbstractRangerHubClient<TestNodeData,
        ListBasedServiceRegistry<TestNodeData>, TestDeserializer<TestNodeData>> {

    @Builder
    public RangerTestHub(String namespace, ObjectMapper mapper, int nodeRefreshTimeMs,
                         Predicate<TestNodeData> criteria, TestDeserializer<TestNodeData> deserilizer) {
        super(namespace, mapper, nodeRefreshTimeMs, criteria, deserilizer);
    }

    @Override
    protected ServiceFinderHub<TestNodeData, ListBasedServiceRegistry<TestNodeData>> buildHub() {
        return new ServiceFinderHubBuilder<TestNodeData, ListBasedServiceRegistry<TestNodeData>>() {
            @Override
            protected void preBuild() {

            }

            @Override
            protected void postBuild(ServiceFinderHub<TestNodeData, ListBasedServiceRegistry<TestNodeData>> serviceFinderHub) {

            }
        }.withServiceDataSource(buildServiceDataSource())
                .withServiceFinderFactory(buildFinderFactory())
                .build();
    }

    @Override
    protected ServiceDataSource buildServiceDataSource() {
        return new StaticDataSource(Sets.newHashSet(RangerHubTestUtils.service));
    }

    @Override
    protected ServiceFinderFactory<TestNodeData, ListBasedServiceRegistry<TestNodeData>> buildFinderFactory() {
        return new TestServiceFinderFactory();
    }
}


