package com.flipkart.ranger.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.TestUtils;
import com.flipkart.ranger.core.finder.ServiceFinder;
import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinder;
import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinderBuilder;
import com.flipkart.ranger.core.finder.nodeselector.RoundRobinServiceNodeSelector;
import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import com.flipkart.ranger.core.finder.shardselector.ListShardSelector;
import com.flipkart.ranger.core.finderhub.*;
import com.flipkart.ranger.core.model.*;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

@Slf4j
public class AbstractHubClientTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Service service = new Service("test-ns", "test-s");

    @Data
    @Builder
    @AllArgsConstructor
    private static class TestShardInfo{
        private int shardId;
    }

    private static class TestCriteria implements Criteria<TestShardInfo>{
        @Override
        public boolean apply(TestShardInfo nodeData) {
            return nodeData.getShardId() == 1;
        }
    }


    @Data
    @Builder
    private static class TestSimpleUnshardedServiceFinder<T, C extends Criteria<T>>
            extends SimpleUnshardedServiceFinderBuilder<TestShardInfo, TestSimpleUnshardedServiceFinder<T, C>, Deserializer<TestShardInfo>, Criteria<TestShardInfo>> {

        @Override
        public SimpleUnshardedServiceFinder<TestShardInfo, Criteria<TestShardInfo>> build() {
            if (null == shardSelector) {
                shardSelector = new ListShardSelector<>();
            }
            final ListBasedServiceRegistry<TestShardInfo> serviceRegistry = new ListBasedServiceRegistry<>(service);
            return new SimpleUnshardedServiceFinder<>(serviceRegistry, shardSelector, nodeSelector);
        }

        @Override
        protected NodeDataSource<TestShardInfo, Deserializer<TestShardInfo>> dataSource(Service service) {
            return new NodeDataSource<TestShardInfo, Deserializer<TestShardInfo>>() {
                @Override
                public Optional<List<ServiceNode<TestShardInfo>>> refresh(Deserializer<TestShardInfo> deserializer) {
                    return Optional.of(
                            Lists.newArrayList(
                                    new ServiceNode<>("localhost", 9200, TestShardInfo.builder().shardId(1).build())
                            )
                    );
                }

                @Override
                public void start() {

                }

                @Override
                public void ensureConnected() {

                }

                @Override
                public void stop() {

                }

                @Override
                public boolean isActive() {
                    return true;
                }
            };
        }
    }

    private static class TestServiceFinderFactory implements ServiceFinderFactory<TestShardInfo, Criteria<TestShardInfo>, ListBasedServiceRegistry<TestShardInfo>>{

        @Override
        public ServiceFinder<TestShardInfo, Criteria<TestShardInfo>, ListBasedServiceRegistry<TestShardInfo>> buildFinder(Service service) {
            final ListBasedServiceRegistry<TestShardInfo> serviceRegistry = new ListBasedServiceRegistry<>(service);
            List<ServiceNode<TestShardInfo>> serviceNodes = Lists.newArrayList();
            serviceNodes.add(new ServiceNode<>("localhost-1", 9000, TestShardInfo.builder().shardId(1).build()));
            serviceRegistry.updateNodes(serviceNodes);

            return new SimpleUnshardedServiceFinder<TestShardInfo, Criteria<TestShardInfo>>(
                    serviceRegistry, new ListShardSelector<>(), new RoundRobinServiceNodeSelector<>()
            );
        }
    }

    private static class TestAbstractHub extends AbstractHubClient<TestShardInfo, Criteria<TestShardInfo>, ListBasedServiceRegistry<TestShardInfo>>{

        public TestAbstractHub(String namespace, ObjectMapper mapper, int refreshTimeMs, Criteria<TestShardInfo> criteria) {
            super(namespace, mapper, refreshTimeMs, criteria);
        }

        @Override
        protected ServiceFinderHub<TestShardInfo, Criteria<TestShardInfo>, ListBasedServiceRegistry<TestShardInfo>> buildHub() {
           return new ServiceFinderHubBuilder<TestShardInfo, Criteria<TestShardInfo>, ListBasedServiceRegistry<TestShardInfo>>() {
                @Override
                protected void preBuild() {

                }

                @Override
                protected void postBuild(ServiceFinderHub<TestShardInfo, Criteria<TestShardInfo>, ListBasedServiceRegistry<TestShardInfo>> serviceFinderHub) {

                }
            }.withServiceDataSource(buildServiceDataSource())
                    .withServiceFinderFactory(buildFinderFactory())
                    .build();
        }

        @Override
        protected ServiceDataSource buildServiceDataSource() {
            return new StaticDataSource(Lists.newArrayList(service));
        }

        @Override
        protected ServiceFinderFactory<TestShardInfo, Criteria<TestShardInfo>, ListBasedServiceRegistry<TestShardInfo>> buildFinderFactory() {
            return new TestServiceFinderFactory();
        }
    }

    @Test
    public void testAbstractHubClient() {
        TestAbstractHub testAbstractHub = new TestAbstractHub(service.getNamespace(), mapper, 1000, new TestCriteria());
        testAbstractHub.start();

        TestUtils.sleepForSeconds(3);

        Optional<ServiceNode<TestShardInfo>> node = testAbstractHub.getNode(service);
        Assert.assertTrue(node.isPresent());
        Assert.assertTrue(node.get().getHost().equalsIgnoreCase("localhost-1"));
        Assert.assertEquals(9000, node.get().getPort());
        Assert.assertEquals(1, node.get().getNodeData().getShardId());
    }
}
