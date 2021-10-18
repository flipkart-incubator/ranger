package com.flipkart.ranger.client.stubs;

import com.flipkart.ranger.core.finder.ServiceFinder;
import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinder;
import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import com.flipkart.ranger.core.finderhub.ServiceFinderFactory;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Deserializer;
import com.flipkart.ranger.core.model.Service;

public class TestServiceFinderFactory  implements ServiceFinderFactory<TestShardInfo, Criteria<TestShardInfo>, ListBasedServiceRegistry<TestShardInfo>> {

    @Override
    public ServiceFinder<TestShardInfo, Criteria<TestShardInfo>, ListBasedServiceRegistry<TestShardInfo>> buildFinder(Service service) {
        SimpleUnshardedServiceFinder<TestShardInfo, Criteria<TestShardInfo>> finder = new TestSimpleUnshardedServiceFinder<TestShardInfo, Criteria<TestShardInfo>>()
                .withNamespace(service.getNamespace())
                .withServiceName(service.getServiceName())
                .withDeserializer(new Deserializer<TestShardInfo>() {
                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }
                })
                .build();
        finder.start();
        return finder;
    }
}

