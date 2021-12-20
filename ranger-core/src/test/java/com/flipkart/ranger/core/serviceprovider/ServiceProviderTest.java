/*
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.ranger.core.serviceprovider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.healthcheck.Healthchecks;
import com.flipkart.ranger.core.model.NodeDataSink;
import com.flipkart.ranger.core.model.Serializer;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import lombok.val;
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

    static class TestNodeDataSink<T extends TestNodeData, S extends TestSerializer<T>> implements NodeDataSink<T, S>{

        public TestNodeDataSink(){
        }

        @Override
        public void updateState(S serializer, ServiceNode<T> serviceNode) {
            testNodeData = serviceNode.getNodeData();
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

    public static class TestServiceProviderBuilder<T extends TestNodeData> extends
            BaseServiceProviderBuilder<T, TestServiceProviderBuilder<T>, TestSerializer<T>> {

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
        new TestServiceProviderBuilder<>()
                .withServiceName("test-service")
                .withNamespace("test")
                .withHostname("localhost-1")
                .withPort(9000)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidServiceProviderNoHealthCheck(){
        new TestServiceProviderBuilder<>()
                .withServiceName("test-service")
                .withNamespace("test")
                .withHostname("localhost-1")
                .withPort(9000)
                .withSerializer(new TestSerializerImpl())
                .build();
    }

    @Test
    public void testBuildServiceProvider(){
        val testProvider = new TestServiceProviderBuilder<>()
                .withServiceName("test-service")
                .withNamespace("test")
                .withHostname("localhost-1")
                .withPort(9000)
                .withSerializer(new TestSerializerImpl())
                .withNodeData(TestNodeData.builder().shardId(1).build())
                .withHealthcheck(Healthchecks.defaultHealthyCheck())
                .withHealthUpdateIntervalMs(1000)
                .build();
        testProvider.start();
        Assert.assertNotNull(testNodeData);
        Assert.assertEquals(1, testNodeData.getShardId());
    }

}
