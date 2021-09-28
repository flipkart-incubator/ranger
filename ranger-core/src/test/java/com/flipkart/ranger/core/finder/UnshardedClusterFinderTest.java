package com.flipkart.ranger.core.finder;

import com.flipkart.ranger.core.finder.serviceregistry.UnshardedClusterServiceRegistry;
import com.flipkart.ranger.core.finder.shardselector.NoOpShardSelector;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.ServiceNodeSelector;
import com.flipkart.ranger.core.model.UnshardedClusterInfo;
import com.flipkart.ranger.core.model.UnshardedCriteria;
import com.flipkart.ranger.core.utils.RegistryTestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UnshardedClusterFinderTest {

    static class TestUnshardedNodeSelector implements ServiceNodeSelector<UnshardedClusterInfo>{

        @Override
        public ServiceNode<UnshardedClusterInfo> select(List<ServiceNode<UnshardedClusterInfo>> serviceNodes) {
            return serviceNodes.get(0);
        }
    }
    @Test
    public void unshardedClusterFinder(){
        final UnshardedClusterServiceRegistry unshardedRegistry = RegistryTestUtils.getUnshardedRegistry();
        final NoOpShardSelector shardSelector = new NoOpShardSelector();
        final UnshardedClusterInfo unshardedClusterInfo = new UnshardedClusterInfo();
        UnshardedClusterFinder unshardedClusterFinder = new UnshardedClusterFinder(
                unshardedRegistry,
                shardSelector,
                new TestUnshardedNodeSelector()
        );
        final ServiceNode<UnshardedClusterInfo> serviceNode = unshardedClusterFinder.get(new UnshardedCriteria(unshardedClusterInfo));
        Assert.assertNotNull(serviceNode);
        Assert.assertEquals("localhost-1", serviceNode.getHost());
    }
}
