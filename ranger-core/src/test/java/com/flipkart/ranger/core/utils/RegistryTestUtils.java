package com.flipkart.ranger.core.utils;

import com.flipkart.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import com.flipkart.ranger.core.finder.serviceregistry.UnshardedClusterServiceRegistry;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.UnshardedClusterInfo;
import com.flipkart.ranger.core.units.TestNodeData;
import com.google.common.collect.Lists;

import java.util.List;

public class RegistryTestUtils {

    public static MapBasedServiceRegistry<TestNodeData> getServiceRegistry(){
        final Service service = new Service("test", "test-service");
        final MapBasedServiceRegistry<TestNodeData> serviceRegistry = new MapBasedServiceRegistry<>(service);
        List<ServiceNode<TestNodeData>> serviceNodes = Lists.newArrayList();
        serviceNodes.add(new ServiceNode<>("localhost-1", 9000, TestNodeData.builder().nodeId(1).build()));
        serviceNodes.add(new ServiceNode<>("localhost-2", 9001, TestNodeData.builder().nodeId(2).build()));
        serviceNodes.add(new ServiceNode<>("localhost-3", 9002, TestNodeData.builder().nodeId(3).build()));
        serviceRegistry.updateNodes(serviceNodes);
        return serviceRegistry;
    }

    public static UnshardedClusterServiceRegistry getUnshardedRegistry(){
        final Service service = new Service("test", "test-service");
        final UnshardedClusterServiceRegistry serviceRegistry = new UnshardedClusterServiceRegistry(service);
        List<ServiceNode<UnshardedClusterInfo>> serviceNodes = Lists.newArrayList();
        serviceNodes.add(new ServiceNode<>("localhost-1", 9000, new UnshardedClusterInfo()));
        serviceNodes.add(new ServiceNode<>("localhost-2", 9001, new UnshardedClusterInfo()));
        serviceNodes.add(new ServiceNode<>("localhost-3", 9002, new UnshardedClusterInfo()));
        serviceRegistry.updateNodes(serviceNodes);
        return serviceRegistry;
    }
}
