package com.flipkart.ranger.finder.unsharded;

import com.flipkart.ranger.finder.AbstractZookeeperServiceRegistry;
import com.flipkart.ranger.finder.Service;
import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.ServiceNode;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UnshardedClusterServiceRegistry extends AbstractZookeeperServiceRegistry<UnshardedClusterInfo> {
    private AtomicReference<List<ServiceNode<UnshardedClusterInfo>>> nodes
                                        = new AtomicReference<List<ServiceNode<UnshardedClusterInfo>>>();

    protected UnshardedClusterServiceRegistry(Service service,
                                              Deserializer<UnshardedClusterInfo> deserializer,
                                              int refreshInterval) {
        super(service, deserializer, refreshInterval);
    }

    public List<ServiceNode<UnshardedClusterInfo>> nodes() {
        return nodes.get();
    }

    @Override
    public void nodes(List<ServiceNode<UnshardedClusterInfo>> serviceNodes) {
        nodes.set(ImmutableList.copyOf(serviceNodes));
    }
}
