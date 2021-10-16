/**
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

package com.flipkart.ranger.zookeeper.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.finder.SimpleShardedServiceFinder;
import com.flipkart.ranger.core.finder.nodeselector.RoundRobinServiceNodeSelector;
import com.flipkart.ranger.core.healthcheck.Healthchecks;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceNode;
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

@Slf4j
public class ServiceProviderTest {

    private TestingCluster testingCluster;
    private ObjectMapper objectMapper;
    private List<ServiceProvider<TestShardInfo, ZkNodeDataSerializer<TestShardInfo>>> serviceProviders = Lists.newArrayList();

    @Before
    public void startTestCluster() throws Exception {
        objectMapper = new ObjectMapper();
        testingCluster = new TestingCluster(3);
        testingCluster.start();
        registerService("localhost-1", 9000, 1);
        registerService("localhost-2", 9000, 1);
        registerService("localhost-3", 9000, 1);
        registerService("localhost-4", 9000, 2);
    }

    @After
    public void stopTestCluster() throws Exception {
        for (ServiceProvider<TestShardInfo, ZkNodeDataSerializer<TestShardInfo>> serviceProvider : serviceProviders) {
            serviceProvider.stop();
        }
        if (null != testingCluster) {
            testingCluster.close();
        }
    }

    private static final class TestShardInfo {
        private int shardId;

        public TestShardInfo(int shardId) {
            this.shardId = shardId;
        }

        public TestShardInfo() {
        }

        public int getShardId() {
            return shardId;
        }

        public void setShardId(int shardId) {
            this.shardId = shardId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TestShardInfo that = (TestShardInfo) o;

            if (shardId != that.shardId) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return shardId;
        }

        private static Criteria<TestShardInfo> getCriteria(int shardId){
            return nodeData -> nodeData.getShardId() == shardId;
        }
    }

    @Test
    public void testBasicDiscovery() throws Exception {
        SimpleShardedServiceFinder<TestShardInfo, Criteria<TestShardInfo>> serviceFinder = ServiceFinderBuilders.<TestShardInfo, Criteria<TestShardInfo>>shardedFinderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withDeserializer(data -> {
                    try {
                        return objectMapper.readValue(data, new TypeReference<ServiceNode<TestShardInfo>>() {});
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .build();
        serviceFinder.start();
        {
            ServiceNode<TestShardInfo> node = serviceFinder.get(TestShardInfo.getCriteria(1));
            Assert.assertNotNull(node);
            Assert.assertEquals(1, node.getNodeData().getShardId());
        }
        {
            ServiceNode<TestShardInfo> node = serviceFinder.get(TestShardInfo.getCriteria(1));
            Assert.assertNotNull(node);
            Assert.assertEquals(1, node.getNodeData().getShardId());
        }
        long startTime = System.currentTimeMillis();
        for (long i = 0; i < 1000000; i++) {
            ServiceNode<TestShardInfo> node = serviceFinder.get(TestShardInfo.getCriteria(2));
            Assert.assertNotNull(node);
            Assert.assertEquals(2, node.getNodeData().getShardId());
        }
        log.info("PERF::RandomSelector::Took (ms):" + (System.currentTimeMillis() - startTime));
        {
            ServiceNode<TestShardInfo> node = serviceFinder.get(TestShardInfo.getCriteria(99));
            Assert.assertNull(node);
        }
        serviceFinder.stop();
        //while (true);
    }

    @Test
    public void testBasicDiscoveryRR() throws Exception {
        SimpleShardedServiceFinder<TestShardInfo, Criteria<TestShardInfo>> serviceFinder
                = ServiceFinderBuilders.<TestShardInfo, Criteria<TestShardInfo>>shardedFinderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withNodeSelector(new RoundRobinServiceNodeSelector<TestShardInfo>())
                .withDeserializer(data -> {
                    try {
                        return objectMapper.readValue(data,
                                                      new TypeReference<ServiceNode<TestShardInfo>>() {
                                                      });
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .build();
        serviceFinder.start();
        {
            ServiceNode<TestShardInfo> node = serviceFinder.get(TestShardInfo.getCriteria(1));
            Assert.assertNotNull(node);
            Assert.assertEquals(1, node.getNodeData().getShardId());
        }
        {
            ServiceNode<TestShardInfo> node = serviceFinder.get(TestShardInfo.getCriteria(1));
            Assert.assertNotNull(node);
            Assert.assertEquals(1, node.getNodeData().getShardId());
        }
        long startTime = System.currentTimeMillis();
        for (long i = 0; i < 1000000; i++) {
            ServiceNode<TestShardInfo> node = serviceFinder.get(TestShardInfo.getCriteria(2));
            Assert.assertNotNull(node);
            Assert.assertEquals(2, node.getNodeData().getShardId());
        }
        log.info("PERF::RoundRobinSelector::Took (ms):" + (System.currentTimeMillis() - startTime));
        {
            ServiceNode<TestShardInfo> node = serviceFinder.get(TestShardInfo.getCriteria(99));
            Assert.assertNull(node);
        }
        serviceFinder.stop();
        //while (true);
    }

    @Test
    public void testVisibility() {
        SimpleShardedServiceFinder<TestShardInfo, Criteria<TestShardInfo>> serviceFinder = ServiceFinderBuilders.
                <TestShardInfo, Criteria<TestShardInfo>>shardedFinderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withNodeSelector(new RoundRobinServiceNodeSelector<TestShardInfo>())
                .withDeserializer(data -> {
                    try {
                        return objectMapper.readValue(data,
                                new TypeReference<ServiceNode<TestShardInfo>>() {
                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .build();
        serviceFinder.start();
        List<ServiceNode<TestShardInfo>> all = serviceFinder.getAll(TestShardInfo.getCriteria(1));
        log.info("Testing ServiceFinder.getAll()");
        for (ServiceNode<TestShardInfo> serviceNode : all) {
            log.info("node = " + serviceNode.getHost() + ":" + serviceNode.getPort() + "  " + serviceNode.getHealthcheckStatus() + " " + serviceNode
                    .getLastUpdatedTimeStamp());
        }
        Assert.assertEquals(3, all.size());
        serviceFinder.stop();
    }

    private void registerService(String host, int port, int shardId) {
        final ServiceProvider<TestShardInfo, ZkNodeDataSerializer<TestShardInfo>> serviceProvider = ServiceProviderBuilders.<TestShardInfo>shardedServiceProviderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withSerializer(data -> {
                    try {
                        return objectMapper.writeValueAsBytes(data);
                    }
                    catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .withHostname(host)
                .withPort(port)
                .withNodeData(new TestShardInfo(shardId))
                .withHealthcheck(Healthchecks.defaultHealthyCheck())
                .withHealthUpdateIntervalMs(15000)
                .build();
        serviceProvider.start();
        serviceProviders.add(serviceProvider);
    }
}