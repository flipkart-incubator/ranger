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
package io.appform.ranger.core.finder.nodeselector;

import io.appform.ranger.core.model.ServiceNode;
import io.appform.ranger.core.units.TestNodeData;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class RoundRobinServiceNodeSelectorTest {
    @Test
    public void testRandomNodeSelector(){
        val roundRobinSelector = new RoundRobinServiceNodeSelector<TestNodeData>();
        val serviceNodes = new ArrayList<ServiceNode<TestNodeData>>();
        serviceNodes.add(ServiceNode.<TestNodeData>builder().host("localhost-1").port(9000).nodeData(TestNodeData.builder().shardId(1).build()).build());
        serviceNodes.add(ServiceNode.<TestNodeData>builder().host("localhost-2").port(9001).nodeData(TestNodeData.builder().shardId(2).build()).build());
        serviceNodes.add(ServiceNode.<TestNodeData>builder().host("localhost-3").port(9002).nodeData(TestNodeData.builder().shardId(3).build()).build());
        Assert.assertEquals("localhost-2", roundRobinSelector.select(serviceNodes).getHost());
        Assert.assertEquals("localhost-3", roundRobinSelector.select(serviceNodes).getHost());
        Assert.assertEquals("localhost-1", roundRobinSelector.select(serviceNodes).getHost());
    }
}
