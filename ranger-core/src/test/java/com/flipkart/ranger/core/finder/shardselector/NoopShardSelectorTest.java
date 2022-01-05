package com.flipkart.ranger.core.finder.shardselector;

import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.utils.RangerTestUtils;
import com.flipkart.ranger.core.utils.RegistryTestUtils;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

public class NoopShardSelectorTest {

    @Test
    public void testNoOpShardSelector(){
        val serviceRegistry = RegistryTestUtils.getUnshardedRegistry();
        val shardSelector = new NoopShardSelector<TestNodeData>();
        val nodes = shardSelector.nodes(RangerTestUtils.getCriteria(1), serviceRegistry);
        Assert.assertFalse(nodes.isEmpty());
        Assert.assertEquals("localhost-1", nodes.get(0).getHost());
    }
}
