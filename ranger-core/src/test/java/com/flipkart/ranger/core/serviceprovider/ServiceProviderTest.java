package com.flipkart.ranger.core.serviceprovider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.healthcheck.Healthchecks;
import com.flipkart.ranger.core.model.NodeDataSink;
import com.flipkart.ranger.core.model.Serializer;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import org.junit.Assert;
import org.junit.Test;

public class ServiceProviderTest {

    static TestNodeData testNodeData = null;

    interface TestSerializer<T> extends Serializer<T> {
        byte[] serialize(final ServiceNode<TestNodeData> node);
    }

    static class TestSerializerImpl implements TestSerializer<TestNodeData>{
        private ObjectMapper objectMapper;

        public TestSerializerImpl(){
            objectMapper = new ObjectMapper();
        }

        @Override
        public byte[] serialize(ServiceNode<TestNodeData> node) {
            try{
                return objectMapper.writeValueAsBytes(node);
            }catch (JsonProcessingException jpe){
                return null;
            }
        }
    }

    static class TestNodeDataSink<T, S extends TestSerializer<T>> implements NodeDataSink<T, S>{

        public TestNodeDataSink(){
        }

        @Override
        public void updateState(S serializer, ServiceNode<T> serviceNode) {
            testNodeData = (TestNodeData) serviceNode.getNodeData();
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
    }

    public class TestServiceProviderBuilder<T> extends BaseServiceProviderBuilder<T, TestServiceProviderBuilder<T>, TestSerializer<T>> {

        @Override
        public ServiceProvider<T, TestSerializer<T>> build() {
            return super.buildProvider();
        }

        @Override
        protected NodeDataSink<T, TestSerializer<T>> dataSink(Service service) {
            return new TestNodeDataSink<>();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testInvalidServiceProvider(){
        new TestServiceProviderBuilder<TestNodeData>()
                .withServiceName("test-service")
                .withNamespace("test")
                .withHostname("localhost-1")
                .withPort(9000)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidServiceProviderNoHealthCheck(){
        new TestServiceProviderBuilder<TestNodeData>()
                .withServiceName("test-service")
                .withNamespace("test")
                .withHostname("localhost-1")
                .withPort(9000)
                .withSerializer(new TestSerializerImpl())
                .build();
    }

    @Test
    public void testBuildServiceProvider(){
        ServiceProvider<TestNodeData, TestSerializer<TestNodeData>> testProvider = new TestServiceProviderBuilder<TestNodeData>()
                .withServiceName("test-service")
                .withNamespace("test")
                .withHostname("localhost-1")
                .withPort(9000)
                .withSerializer(new TestSerializerImpl())
                .withNodeData(TestNodeData.builder().nodeId(1).build())
                .withHealthcheck(Healthchecks.defaultHealthyCheck())
                .withHealthUpdateIntervalMs(1000)
                .build();
        testProvider.start();
        Assert.assertNotNull(testNodeData);
        Assert.assertEquals(1, testNodeData.getNodeId());
    }

}
