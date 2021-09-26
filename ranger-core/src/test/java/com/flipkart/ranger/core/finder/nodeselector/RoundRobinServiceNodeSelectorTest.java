package com.flipkart.ranger.core.finder.nodeselector;

import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class RoundRobinServiceNodeSelectorTest {
    @Test
    public void testRandomNodeSelector(){
        final RoundRobinServiceNodeSelector<TestNodeData> roundRobinSelector = new RoundRobinServiceNodeSelector<TestNodeData>();
        List<ServiceNode<TestNodeData>> serviceNodes = Lists.newArrayList();
        serviceNodes.add(new ServiceNode<>("localhost-1", 9000, TestNodeData.builder().nodeId(1).build()));
        serviceNodes.add(new ServiceNode<>("localhost-2", 9001, TestNodeData.builder().nodeId(2).build()));
        serviceNodes.add(new ServiceNode<>("localhost-3", 9002, TestNodeData.builder().nodeId(3).build()));
        ServiceNode<TestNodeData> select = roundRobinSelector.select(serviceNodes);
        Assert.assertTrue(select.getHost().equalsIgnoreCase("localhost-2"));
        select = roundRobinSelector.select(serviceNodes);
        Assert.assertTrue(select.getHost().equalsIgnoreCase("localhost-3"));
        select = roundRobinSelector.select(serviceNodes);
        Assert.assertTrue(select.getHost().equalsIgnoreCase("localhost-1"));
    }
}
