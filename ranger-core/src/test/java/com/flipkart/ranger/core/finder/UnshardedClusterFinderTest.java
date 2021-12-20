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
package com.flipkart.ranger.core.finder;

import com.flipkart.ranger.core.finder.shardselector.ListShardSelector;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.ServiceNodeSelector;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.utils.RangerTestUtils;
import com.flipkart.ranger.core.utils.RegistryTestUtils;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UnshardedClusterFinderTest {

    static class TestUnshardedNodeSelector implements ServiceNodeSelector<TestNodeData>{

        @Override
        public ServiceNode<TestNodeData> select(List<ServiceNode<TestNodeData>> serviceNodes) {
            return serviceNodes.get(0);
        }
    }
    @Test
    public void unshardedClusterFinder(){
        val unshardedRegistry = RegistryTestUtils.getUnshardedRegistry();
        val shardSelector = new ListShardSelector<TestNodeData>();
        val simpleUnshardedServiceFinder = new SimpleUnshardedServiceFinder<>(
                unshardedRegistry,
                shardSelector,
                new TestUnshardedNodeSelector()
        );
        val serviceNode = simpleUnshardedServiceFinder.get(RangerTestUtils.getCriteria(1));
        Assert.assertTrue(serviceNode.isPresent());
        Assert.assertEquals("localhost-1", serviceNode.get().getHost());
    }
}
