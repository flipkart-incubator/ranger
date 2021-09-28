package com.flipkart.ranger.core.finder.shardselector;

import com.flipkart.ranger.core.finder.serviceregistry.UnshardedClusterServiceRegistry;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.UnshardedClusterInfo;
import com.flipkart.ranger.core.model.UnshardedCriteria;
import com.flipkart.ranger.core.utils.RegistryTestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class NoopShardSelectorTest {

    @Test
    public void testNoOpShardSelector(){
        final UnshardedClusterServiceRegistry serviceRegistry = RegistryTestUtils.getUnshardedRegistry();
        final NoOpShardSelector shardSelector = new NoOpShardSelector();
        final UnshardedClusterInfo unshardedClusterInfo = new UnshardedClusterInfo();
        final List<ServiceNode<UnshardedClusterInfo>> nodes = shardSelector.nodes(new UnshardedCriteria(unshardedClusterInfo), serviceRegistry);
        Assert.assertFalse(nodes.isEmpty());
        Assert.assertEquals("localhost-1", nodes.get(0).getHost());
    }
}
