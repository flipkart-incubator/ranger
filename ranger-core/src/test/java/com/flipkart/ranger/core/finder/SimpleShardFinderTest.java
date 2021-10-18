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

import com.flipkart.ranger.core.finder.nodeselector.RoundRobinServiceNodeSelector;
import com.flipkart.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.ShardSelector;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.utils.CriteriaUtils;
import com.flipkart.ranger.core.utils.RegistryTestUtils;
import com.google.common.collect.Lists;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SimpleShardFinderTest {

    static class TestSimpleShardSelector<T, C extends Criteria<T>> implements ShardSelector<T, C, MapBasedServiceRegistry<T>>{

        @Override
        public List<ServiceNode<T>> nodes(C criteria, MapBasedServiceRegistry<T> serviceRegistry) {
            return Lists.newArrayList();
        }
    }

    @Test
    public void testSimpleShardedFinder(){
        final MapBasedServiceRegistry<TestNodeData> serviceRegistry = RegistryTestUtils.getServiceRegistry();
        final TestSimpleShardSelector<TestNodeData, Criteria<TestNodeData>> shardSelector = new TestSimpleShardSelector<>();
        final RoundRobinServiceNodeSelector<TestNodeData> roundRobinServiceNodeSelector = new RoundRobinServiceNodeSelector<>();
        val simpleShardedFinder = new SimpleShardedServiceFinder<>(
                serviceRegistry, shardSelector, roundRobinServiceNodeSelector);
        val testNodeDataServiceNode = simpleShardedFinder.get(
                CriteriaUtils.getCriteria(2));
        Assert.assertNull(testNodeDataServiceNode);
    }
}
