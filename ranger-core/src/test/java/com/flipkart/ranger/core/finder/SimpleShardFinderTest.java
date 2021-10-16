package com.flipkart.ranger.core.finder;

import com.flipkart.ranger.core.finder.nodeselector.RoundRobinServiceNodeSelector;
import com.flipkart.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.ServiceRegistry;
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
