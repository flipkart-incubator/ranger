package com.flipkart.ranger.finder.sharded;

import com.flipkart.ranger.finder.AbstractZookeeperServiceRegistry;
import com.flipkart.ranger.finder.Service;
import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.ServiceNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MapBasedServiceRegistry<T> extends AbstractZookeeperServiceRegistry<T> {
    private AtomicReference<ListMultimap<T,ServiceNode<T>>> nodes = new AtomicReference<ListMultimap<T, ServiceNode<T>>>();

    public MapBasedServiceRegistry(Service service, Deserializer<T> deserializer) {
        super(service, deserializer);
    }

    public ListMultimap<T, ServiceNode<T>> nodes() {
        return nodes.get();
    }

    @Override
    public void nodes(List<ServiceNode<T>> nodes) {
        ListMultimap<T, ServiceNode<T>> serviceNodes = ArrayListMultimap.create();
        for(ServiceNode<T> serviceNode : nodes) {
            serviceNodes.put(serviceNode.getNodeData(), serviceNode);
        }
        this.nodes.set(ImmutableListMultimap.copyOf(serviceNodes));
    }

}
