package com.flipkart.ranger.core.finder.shardselector;

import com.flipkart.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.utils.CriteriaUtils;
import com.flipkart.ranger.core.utils.RegistryTestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class MatchingShardSelectorTest {

    @Test
    public void testMatchingShardSelector(){
        final MapBasedServiceRegistry<TestNodeData> serviceRegistry = RegistryTestUtils.getServiceRegistry();
        final MatchingShardSelector<TestNodeData, Criteria<TestNodeData>> shardSelector = new MatchingShardSelector<>();
        final List<ServiceNode<TestNodeData>> nodes = shardSelector.nodes(
                CriteriaUtils.getCriteria(1), serviceRegistry);
        Assert.assertFalse(nodes.isEmpty());
        Assert.assertEquals("localhost-1", nodes.get(0).getHost());
    }
}
