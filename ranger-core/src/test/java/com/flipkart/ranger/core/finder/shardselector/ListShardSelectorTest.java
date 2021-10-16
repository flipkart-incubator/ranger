package com.flipkart.ranger.core.finder.shardselector;

import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.utils.CriteriaUtils;
import com.flipkart.ranger.core.utils.RegistryTestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ListShardSelectorTest {

    @Test
    public void testNoOpShardSelector(){
        final ListBasedServiceRegistry serviceRegistry = RegistryTestUtils.getUnshardedRegistry();
        final ListShardSelector<TestNodeData, Criteria<TestNodeData>> shardSelector = new ListShardSelector<>();
        final List<ServiceNode<TestNodeData>> nodes = shardSelector.nodes(CriteriaUtils.getCriteria(1), serviceRegistry);
        Assert.assertFalse(nodes.isEmpty());
        Assert.assertEquals("localhost-1", nodes.get(0).getHost());
    }
}
