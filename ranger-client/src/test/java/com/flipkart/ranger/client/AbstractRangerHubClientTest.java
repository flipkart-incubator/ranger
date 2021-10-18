package com.flipkart.ranger.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.TestUtils;
import com.flipkart.ranger.core.finder.ServiceFinder;
import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinder;
import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinderBuilder;
import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
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
public class AbstractRangerHubClientTest {

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
            return buildFinder();
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

    private static class TestDeserilizer<T> implements Deserializer<T>{

    }

    private static class TestAbstractRangerHub extends AbstractRangerHubClient<TestShardInfo, Criteria<TestShardInfo>, ListBasedServiceRegistry<TestShardInfo>, TestDeserilizer<TestShardInfo>> {

        public TestAbstractRangerHub(String namespace, ObjectMapper mapper, int refreshTimeMs, Criteria<TestShardInfo> criteria, TestDeserilizer<TestShardInfo> deserilizer) {
            super(namespace, mapper, refreshTimeMs, criteria, deserilizer);
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
        TestAbstractRangerHub testAbstractHub = new TestAbstractRangerHub(service.getNamespace(), mapper, 1000, new TestCriteria(), new TestDeserilizer<>());
        testAbstractHub.start();

        TestUtils.sleepForSeconds(3);

        Optional<ServiceNode<TestShardInfo>> node = testAbstractHub.getNode(service);
        Assert.assertTrue(node.isPresent());
        Assert.assertTrue(node.get().getHost().equalsIgnoreCase("localhost"));
        Assert.assertEquals(9200, node.get().getPort());
        Assert.assertEquals(1, node.get().getNodeData().getShardId());

        node = testAbstractHub.getNode(new Service("test", "test"));
        Assert.assertFalse(node.isPresent());

        node = testAbstractHub.getNode(service, nodeData -> nodeData.getShardId() == 2);
        Assert.assertFalse(node.isPresent());

        node = testAbstractHub.getNode(new Service("test", "test"), nodeData -> nodeData.getShardId() == 1);
        Assert.assertFalse(node.isPresent());

        testAbstractHub.start();
    }
}
