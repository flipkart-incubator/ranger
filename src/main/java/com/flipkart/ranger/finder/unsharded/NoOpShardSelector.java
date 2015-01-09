package com.flipkart.ranger.finder.unsharded;

import com.flipkart.ranger.finder.unsharded.UnshardedClusterInfo;
import com.flipkart.ranger.finder.unsharded.UnshardedClusterServiceRegistry;
import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.model.ShardSelector;

import java.util.List;

public class NoOpShardSelector implements ShardSelector<UnshardedClusterInfo, UnshardedClusterServiceRegistry> {
    @Override
    public List<ServiceNode<UnshardedClusterInfo>> nodes(UnshardedClusterInfo criteria,
                                                         UnshardedClusterServiceRegistry serviceRegistry) {
        return serviceRegistry.nodes();
    }
}
