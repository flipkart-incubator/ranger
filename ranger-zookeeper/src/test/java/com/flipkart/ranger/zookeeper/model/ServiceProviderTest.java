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

package com.flipkart.ranger.zookeeper.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.finder.nodeselector.RoundRobinServiceNodeSelector;
import com.flipkart.ranger.core.healthcheck.Healthchecks;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.serviceprovider.ServiceProvider;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.utils.RangerTestUtils;
import com.flipkart.ranger.zookeeper.ServiceFinderBuilders;
import com.flipkart.ranger.zookeeper.ServiceProviderBuilders;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataSerializer;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.LongStream;

@Slf4j
public class ServiceProviderTest {

    private TestingCluster testingCluster;
    private ObjectMapper objectMapper;
    private List<ServiceProvider<TestNodeData, ZkNodeDataSerializer<TestNodeData>>> serviceProviders = Lists.newArrayList();

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
        serviceProviders.forEach(ServiceProvider::stop);
        if (null != testingCluster) {
            testingCluster.close();
        }
    }

    @Test
    public void testBasicDiscovery() {
        val serviceFinder = ServiceFinderBuilders.<TestNodeData>shardedFinderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withDeserializer(data -> {
                    try {
                        return objectMapper.readValue(data, new TypeReference<ServiceNode<TestNodeData>>() {});
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .build();
        serviceFinder.start();
        {
            val node = serviceFinder.get(RangerTestUtils.getCriteria(1)).orElse(null);
            Assert.assertNotNull(node);
            Assert.assertEquals(1, node.getNodeData().getShardId());
        }
        {
            val node = serviceFinder.get(RangerTestUtils.getCriteria(1)).orElse(null);
            Assert.assertNotNull(node);
            Assert.assertEquals(1, node.getNodeData().getShardId());
        }
        val startTime = System.currentTimeMillis();
        LongStream.range(0, 1000000).mapToObj(i -> serviceFinder.get(RangerTestUtils.getCriteria(2)).orElse(null)).forEach(node -> {
            Assert.assertNotNull(node);
            Assert.assertEquals(2, node.getNodeData().getShardId());
        });
        log.info("PERF::RandomSelector::Took (ms):" + (System.currentTimeMillis() - startTime));
        {
            val node = serviceFinder.get(RangerTestUtils.getCriteria(99)).orElse(null);
            Assert.assertNull(node);
        }
        serviceFinder.stop();
    }

    @Test
    public void testBasicDiscoveryRR() {
        val serviceFinder
                = ServiceFinderBuilders.<TestNodeData>shardedFinderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withNodeSelector(new RoundRobinServiceNodeSelector<>())
                .withDeserializer(data -> {
                    try {
                        return objectMapper.readValue(data,
                                new TypeReference<ServiceNode<TestNodeData>>() {
                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .build();
        serviceFinder.start();
        {
            val node = serviceFinder.get(RangerTestUtils.getCriteria(1));
            Assert.assertTrue(node.isPresent());
            Assert.assertEquals(1, node.get().getNodeData().getShardId());
        }
        {
            val node = serviceFinder.get(RangerTestUtils.getCriteria(1));
            Assert.assertTrue(node.isPresent());
            Assert.assertEquals(1, node.get().getNodeData().getShardId());
        }
        long startTime = System.currentTimeMillis();
        LongStream.range(0, 1000000).mapToObj(i -> serviceFinder.get(RangerTestUtils.getCriteria(2))).forEach(node -> {
            Assert.assertTrue(node.isPresent());
            Assert.assertEquals(2, node.get().getNodeData().getShardId());
        });
        log.info("PERF::RoundRobinSelector::Took (ms):" + (System.currentTimeMillis() - startTime));
        {
            val node = serviceFinder.get(RangerTestUtils.getCriteria(99));
            Assert.assertFalse(node.isPresent());
        }
        serviceFinder.stop();
        //while (true);
    }

    @Test
    public void testVisibility() {
        val serviceFinder = ServiceFinderBuilders.
                <TestNodeData>shardedFinderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withNodeSelector(new RoundRobinServiceNodeSelector<>())
                .withDeserializer(data -> {
                    try {
                        return objectMapper.readValue(data,
                                new TypeReference<ServiceNode<TestNodeData>>() {
                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .build();
        serviceFinder.start();
        val all = serviceFinder.getAll(RangerTestUtils.getCriteria(1));
        log.info("Testing ServiceFinder.getAll()");
        all.stream().map(serviceNode -> "node = " + serviceNode.getHost() + ":" + serviceNode.getPort() + "  " + serviceNode.getHealthcheckStatus() + " " + serviceNode
                .getLastUpdatedTimeStamp()).forEach(log::info);
        Assert.assertEquals(3, all.size());
        serviceFinder.stop();
    }

    private void registerService(String host, int port, int shardId) {
        val serviceProvider = ServiceProviderBuilders.<TestNodeData>shardedServiceProviderBuilder()
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
                .withNodeData(TestNodeData.builder().shardId(shardId).build())
                .withHealthcheck(Healthchecks.defaultHealthyCheck())
                .withHealthUpdateIntervalMs(15000)
                .build();
        serviceProvider.start();
        serviceProviders.add(serviceProvider);
    }
}