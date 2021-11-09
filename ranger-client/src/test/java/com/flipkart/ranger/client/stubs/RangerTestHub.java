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
import com.flipkart.ranger.core.model.Criteria;
import com.google.common.collect.Lists;

public class RangerTestHub extends AbstractRangerHubClient<TestShardInfo, Criteria<TestShardInfo>, ListBasedServiceRegistry<TestShardInfo>, TestDeserializer<TestShardInfo>> {

    public RangerTestHub(String namespace, ObjectMapper mapper, int nodeRefreshTimeMs, Criteria<TestShardInfo> criteria, TestDeserializer<TestShardInfo> deserilizer) {
        super(namespace, mapper, nodeRefreshTimeMs, criteria, deserilizer);
    }

    @Override
    protected ServiceFinderHub<TestShardInfo, Criteria<TestShardInfo>, ListBasedServiceRegistry<TestShardInfo>> buildHub() {
        return new ServiceFinderHubBuilder<TestShardInfo, Criteria<TestShardInfo>, ListBasedServiceRegistry<TestShardInfo>>() {
            @Override
            protected void preBuild() {

            }

            @Override
            protected void postBuild(ServiceFinderHub<TestShardInfo, Criteria<TestShardInfo>, ListBasedServiceRegistry<TestShardInfo>> serviceFinderHub) {

            }
        }.withServiceDataSource(buildServiceDataSource())
                .withServiceFinderFactory(buildFinderFactory())
                .build();
    }

    @Override
    protected ServiceDataSource buildServiceDataSource() {
        return new StaticDataSource(Lists.newArrayList(RangerHubTestUtils.service));
    }

    @Override
    protected ServiceFinderFactory<TestShardInfo, Criteria<TestShardInfo>, ListBasedServiceRegistry<TestShardInfo>> buildFinderFactory() {
        return new TestServiceFinderFactory();
    }
}


