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
import com.flipkart.ranger.finder.CuratorSourceConfig;
import com.flipkart.ranger.finder.sharded.SimpleShardedServiceFinder;
import com.flipkart.ranger.healthcheck.Healthchecks;
import com.flipkart.ranger.serviceprovider.ServiceProvider;
import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ServiceProviderExtCuratorTest {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderExtCuratorTest.class);

    private TestingCluster testingCluster;
    private ObjectMapper objectMapper;
    private List<ServiceProvider<TestShardInfo>> serviceProviders = Lists.newArrayList();
    private CuratorFramework curatorFramework;

    @Before
    public void startTestCluster() throws Exception {
        objectMapper = new ObjectMapper();
        testingCluster = new TestingCluster(3);
        testingCluster.start();
        curatorFramework = CuratorFrameworkFactory.builder()
                .namespace("test")
                .connectString(testingCluster.getConnectString())
                .retryPolicy(new ExponentialBackoffRetry(1000, 100)).build();
        curatorFramework.start();
        registerService("localhost-1", 9000, 1);
        registerService("localhost-2", 9000, 1);
        registerService("localhost-3", 9000, 2);
    }

    @After
    public void stopTestCluster() throws Exception {
        for(ServiceProvider<TestShardInfo> serviceProvider : serviceProviders) {
            serviceProvider.stop();
        }
        curatorFramework.close();
        if(null != testingCluster) {
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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestShardInfo that = (TestShardInfo) o;

            if (shardId != that.shardId) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return shardId;
        }
    }

    @Test
    public void testBasicDiscovery() throws Exception {
        CuratorSourceConfig curatorSourceConfig = new CuratorSourceConfig(null, "test");
        SimpleShardedServiceFinder<TestShardInfo> serviceFinder = ServiceFinderBuilders.<TestShardInfo>shardedFinderBuilder()
                .withCuratorFramework(curatorFramework)
                .withCuratorSourceConfig(curatorSourceConfig)
                .withServiceName("test-service")
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
            ServiceNode<TestShardInfo> node = serviceFinder.get(new TestShardInfo(1));
            Assert.assertNotNull(node);
            Assert.assertEquals(1, node.getNodeData().getShardId());
        }
        {
            ServiceNode<TestShardInfo> node = serviceFinder.get(new TestShardInfo(1));
            Assert.assertNotNull(node);
            Assert.assertEquals(1, node.getNodeData().getShardId());
        }
        long startTime = System.currentTimeMillis();
        for(long i = 0; i <1000000; i++)
        {
            ServiceNode<TestShardInfo> node = serviceFinder.get(new TestShardInfo(2));
            Assert.assertNotNull(node);
            Assert.assertEquals(2, node.getNodeData().getShardId());
        }
        logger.info("PERF::RandomSelector::Took (ms):" + (System.currentTimeMillis() - startTime));
        {
            ServiceNode<TestShardInfo> node = serviceFinder.get(new TestShardInfo(99));
            Assert.assertNull(node);
        }
        serviceFinder.stop();
        //while (true);
    }

    private void registerService(String host, int port, int shardId) throws Exception {
        final ServiceProvider<TestShardInfo> serviceProvider = ServiceProviderBuilders.<TestShardInfo>shardedServiceProviderBuilder()
                .withCuratorFramework(curatorFramework)
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
                .withNodeData(new TestShardInfo(shardId))
                .withHealthcheck(Healthchecks.defaultHealthyCheck())
                .buildServiceDiscovery();
        serviceProvider.start();
        serviceProviders.add(serviceProvider);
    }
}