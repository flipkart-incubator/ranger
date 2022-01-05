/*
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
package io.appform.ranger.core.utils;

import io.appform.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import io.appform.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import io.appform.ranger.core.model.ServiceNode;
import io.appform.ranger.core.units.TestNodeData;
import com.google.common.collect.ImmutableList;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class RegistryTestUtils {

    public static MapBasedServiceRegistry<TestNodeData> getServiceRegistry(){
        val serviceRegistry = new MapBasedServiceRegistry<TestNodeData>(RangerTestUtils.service);
        val serviceNodes = ImmutableList.of(
                ServiceNode.<TestNodeData>builder().host("localhost-1").port(9000).nodeData(TestNodeData.builder().shardId(1).build()).build(),
                ServiceNode.<TestNodeData>builder().host("localhost-2").port(9001).nodeData(TestNodeData.builder().shardId(2).build()).build(),
                ServiceNode.<TestNodeData>builder().host("localhost-3").port(9002).nodeData(TestNodeData.builder().shardId(3).build()).build()
        );
        serviceRegistry.updateNodes(serviceNodes);
        return serviceRegistry;
    }

    public static ListBasedServiceRegistry<TestNodeData> getUnshardedRegistry(){
        val serviceRegistry = new ListBasedServiceRegistry<TestNodeData>(RangerTestUtils.service);
        val serviceNodes = ImmutableList.of(
                ServiceNode.<TestNodeData>builder().host("localhost-1").port(9000).nodeData(TestNodeData.builder().shardId(1).build()).build(),
                ServiceNode.<TestNodeData>builder().host("localhost-2").port(9001).nodeData(TestNodeData.builder().shardId(2).build()).build(),
                ServiceNode.<TestNodeData>builder().host("localhost-3").port(9002).nodeData(TestNodeData.builder().shardId(3).build()).build()
        );
        serviceRegistry.updateNodes(serviceNodes);
        return serviceRegistry;
    }
}
