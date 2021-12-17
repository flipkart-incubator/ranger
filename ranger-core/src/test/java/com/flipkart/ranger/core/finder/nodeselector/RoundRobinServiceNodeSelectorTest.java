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
package com.flipkart.ranger.core.finder.nodeselector;

import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class RoundRobinServiceNodeSelectorTest {
    @Test
    public void testRandomNodeSelector(){
        val roundRobinSelector = new RoundRobinServiceNodeSelector<TestNodeData>();
        val serviceNodes = new ArrayList<ServiceNode<TestNodeData>>();
        serviceNodes.add(new ServiceNode<>("localhost-1", 9000, TestNodeData.builder().shardId(1).build()));
        serviceNodes.add(new ServiceNode<>("localhost-2", 9001, TestNodeData.builder().shardId(2).build()));
        serviceNodes.add(new ServiceNode<>("localhost-3", 9002, TestNodeData.builder().shardId(3).build()));
        ServiceNode<TestNodeData> select = roundRobinSelector.select(serviceNodes);
        Assert.assertEquals("localhost-2", select.getHost());
        select = roundRobinSelector.select(serviceNodes);
        Assert.assertEquals("localhost-3", select.getHost());
        select = roundRobinSelector.select(serviceNodes);
        Assert.assertEquals("localhost-1", select.getHost());
    }
}
