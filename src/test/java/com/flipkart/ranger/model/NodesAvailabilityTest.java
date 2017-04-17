/**
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.ranger.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.ServiceFinderBuilders;
import com.flipkart.ranger.ServiceProviderBuilders;
import com.flipkart.ranger.finder.sharded.MapBasedServiceRegistry;
import com.flipkart.ranger.finder.sharded.SimpleShardedServiceFinder;
import com.flipkart.ranger.healthcheck.Healthcheck;
import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.serviceprovider.ServiceProvider;
import com.google.common.collect.Lists;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class NodesAvailabilityTest {
    private TestingCluster testingCluster;
    private ObjectMapper objectMapper;
    private List<ServiceProvider<TestShardInfo>> serviceProviders = Lists.newArrayList();

    @Before
    public void startTestCluster() throws Exception {
        objectMapper = new ObjectMapper();
        testingCluster = new TestingCluster(3);
        testingCluster.start();
        registerService("localhost-1", 9000, 1, 2, HealthcheckStatus.unhealthy);
        registerService("localhost-2", 9000, 1, 2, HealthcheckStatus.unhealthy);
        registerService("localhost-3", 9000, 1, 2, HealthcheckStatus.down);
    }

    @After
    public void stopTestCluster() throws Exception {
        for(ServiceProvider<TestShardInfo> serviceProvider : serviceProviders) {
            serviceProvider.stop();
        }
        if(null != testingCluster) {
            testingCluster.close();
        }
    }

    private static final class TestShardInfo {
        private int a;
        private int b;

        public TestShardInfo(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public TestShardInfo() {
        }

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestShardInfo that = (TestShardInfo) o;

            return a == that.a && b == that.b;

        }

        @Override
        public int hashCode() {
            int result = a;
            result = 31 * result + b;
            return result;
        }
    }

    private static final class TestShardSelector extends ShardSelector<TestShardInfo, MapBasedServiceRegistry<TestShardInfo>> {

        public TestShardSelector(int minNodesAvailable) {
            super(minNodesAvailable);
        }

        @Override
        public List<ServiceNode<TestShardInfo>> nodes(TestShardInfo criteria, MapBasedServiceRegistry<TestShardInfo> serviceRegistry) {
            List<ServiceNode<TestShardInfo>> nodes = Lists.newArrayList();
            for(Map.Entry<TestShardInfo, ServiceNode<TestShardInfo>> entry : serviceRegistry.nodes().entries()) {
                TestShardInfo shardInfo = entry.getKey();
                if((shardInfo.getA() + shardInfo.getB()) == (criteria.getA() + criteria.getB())) {
                    nodes.add(entry.getValue());
                }
            }
            return getServiceableNodes(nodes);
        }
    }

    @Test
    public void testBasicDiscovery() throws Exception {
        SimpleShardedServiceFinder<TestShardInfo> serviceFinder = ServiceFinderBuilders.<TestShardInfo>shardedFinderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withShardSelector(new TestShardSelector(2))
                .withDeserializer(new Deserializer<TestShardInfo>() {
                    @Override
                    public ServiceNode<TestShardInfo> deserialize(byte[] data) {
                        try {
                            return objectMapper.readValue(data,
                                    new TypeReference<ServiceNode<TestShardInfo>>() {
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .build();
        serviceFinder.start();


        {
            ServiceNode<TestShardInfo> node = serviceFinder.get(new TestShardInfo(1, 2));
            Assert.assertNotNull(node);
        }
        serviceFinder.stop();
    }

    private void registerService(String host, int port, int a, int b, final HealthcheckStatus healthStatus) throws Exception {
        final ServiceProvider<TestShardInfo> serviceProvider = ServiceProviderBuilders.<TestShardInfo>shardedServiceProviderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withSerializer(new Serializer<TestShardInfo>() {
                    @Override
                    public byte[] serialize(ServiceNode<TestShardInfo> data) {
                        try {
                            return objectMapper.writeValueAsBytes(data);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .withHostname(host)
                .withPort(port)
                .withNodeData(new TestShardInfo(a,b))
                .withHealthcheck(new Healthcheck() {
                    @Override
                    public HealthcheckStatus check() {
                        return healthStatus;
                    }
                })
                .buildServiceDiscovery();
        serviceProvider.start();
        serviceProviders.add(serviceProvider);
    }
}