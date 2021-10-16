package com.flipkart.ranger.core.finder.serviceregistry;

import com.flipkart.ranger.core.finder.shardselector.MatchingShardSelector;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.utils.CriteriaUtils;
import com.flipkart.ranger.core.utils.RegistryTestUtils;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

public class MapBasedServiceRegistryTest {

    @Test
    public void testMapBasedServiceRegistryWithMatchingShardSelector(){
        final MapBasedServiceRegistry<TestNodeData> serviceRegistry = RegistryTestUtils.getServiceRegistry();
        Assert.assertTrue(null != serviceRegistry.nodes() && !serviceRegistry.nodes().isEmpty());
        final MatchingShardSelector<TestNodeData, Criteria<TestNodeData>> matchingShardSelector = new MatchingShardSelector<>();
        val nodes = matchingShardSelector.nodes(
                CriteriaUtils.getCriteria(1), serviceRegistry);
        Assert.assertFalse(nodes.isEmpty());
        Assert.assertEquals("localhost-1", nodes.get(0).getHost());
    }

}
