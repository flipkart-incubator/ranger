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

package com.flipkart.ranger.zookeeper.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.finder.SimpleShardedServiceFinder;
import com.flipkart.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import com.flipkart.ranger.core.healthcheck.Healthchecks;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.ShardSelector;
import com.flipkart.ranger.core.model.ShardedCriteria;
import com.flipkart.ranger.core.serviceprovider.ServiceProvider;
import com.flipkart.ranger.zookeeper.ServiceFinderBuilders;
import com.flipkart.ranger.zookeeper.ServiceProviderBuilders;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataSerializer;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Slf4j
public class CustomShardSelectorTest {
    private TestingCluster testingCluster;
    private ObjectMapper objectMapper;
    private List<ServiceProvider<TestShardInfo, ZkNodeDataSerializer<TestShardInfo>>> serviceProviders = Lists.newArrayList();

    @Before
    public void startTestCluster() throws Exception {
        objectMapper = new ObjectMapper();
        testingCluster = new TestingCluster(3);
        testingCluster.start();
        registerService("localhost-1", 9000, 1, 2);
        registerService("localhost-2", 9000, 1, 3);
        registerService("localhost-3", 9000, 2, 3);
    }

    @After
    public void stopTestCluster() throws Exception {
        for(ServiceProvider<TestShardInfo, ZkNodeDataSerializer<TestShardInfo>> serviceProvider : serviceProviders) {
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

        public void setA(int a) {
            this.a = a;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
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

        private static ShardedCriteria<TestShardInfo> getCriteria(int a, int b){
            return () -> new TestShardInfo(a, b);
        }
    }

    private static final class TestShardSelector implements ShardSelector<TestShardInfo, MapBasedServiceRegistry<TestShardInfo>, ShardedCriteria<TestShardInfo>> {

        @Override
        public List<ServiceNode<TestShardInfo>> nodes(
                ShardedCriteria<TestShardInfo> criteria,
                MapBasedServiceRegistry<TestShardInfo> serviceRegistry
        ) {
            List<ServiceNode<TestShardInfo>> nodes = Lists.newArrayList();
            for(Map.Entry<TestShardInfo, ServiceNode<TestShardInfo>> entry : serviceRegistry.nodes().entries()) {
                TestShardInfo shardInfo = entry.getKey();
                if((shardInfo.getA() + shardInfo.getB()) == (criteria.getShard().getA() + criteria.getShard().getB())) {
                    nodes.add(entry.getValue());
                }
            }
            return nodes;
        }
    }

    @Test
    public void testBasicDiscovery() throws Exception {
        SimpleShardedServiceFinder<TestShardInfo> serviceFinder = ServiceFinderBuilders.<TestShardInfo>shardedFinderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withShardSelector(new TestShardSelector())
                .withDeserializer(data -> {
                    try {
                        return objectMapper.readValue(data,
                                new TypeReference<ServiceNode<TestShardInfo>>() {});
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .build();
        serviceFinder.start();
        {
            ServiceNode<TestShardInfo> node = serviceFinder.get(TestShardInfo.getCriteria(1, 10));
            Assert.assertNull(node);
        }
        {
            ServiceNode<TestShardInfo> node = serviceFinder.get(TestShardInfo.getCriteria(1, 2));
            Assert.assertNotNull(node);
            Assert.assertEquals(new TestShardInfo(1, 2), node.getNodeData());
        }
        serviceFinder.stop();
    }

    private void registerService(String host, int port, int a, int b) {
        final ServiceProvider<TestShardInfo, ZkNodeDataSerializer<TestShardInfo>> serviceProvider = ServiceProviderBuilders.<TestShardInfo>shardedServiceProviderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withSerializer(data -> {
                    try {
                        return objectMapper.writeValueAsBytes(data);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .withHostname(host)
                .withPort(port)
                .withNodeData(new TestShardInfo(a,b))
                .withHealthcheck(Healthchecks.defaultHealthyCheck())
                .withHealthUpdateIntervalMs(1000)
                .build();
        serviceProvider.start();
        serviceProviders.add(serviceProvider);
    }
}