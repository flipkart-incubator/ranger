package com.flipkart.ranger.core.finder;

import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import com.flipkart.ranger.core.finder.shardselector.FilterShardSelector;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.ServiceNodeSelector;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.utils.CriteriaUtils;
import com.flipkart.ranger.core.utils.RegistryTestUtils;
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
        final ListBasedServiceRegistry<TestNodeData> unshardedRegistry = RegistryTestUtils.getUnshardedRegistry();
        final FilterShardSelector<TestNodeData> shardSelector = new FilterShardSelector<>();
        SimpleUnshardedServiceFinder<TestNodeData> simpleUnshardedServiceFinder = new SimpleUnshardedServiceFinder<TestNodeData>(
                unshardedRegistry,
                shardSelector,
                new TestUnshardedNodeSelector()
        );
        final ServiceNode<TestNodeData> serviceNode = simpleUnshardedServiceFinder.get(CriteriaUtils.getUnshardedCriteria(1));
        Assert.assertNotNull(serviceNode);
        Assert.assertEquals("localhost-1", serviceNode.getHost());
    }
}
