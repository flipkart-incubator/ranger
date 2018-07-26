/**
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
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
import com.flipkart.ranger.healthcheck.Healthcheck;
import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.serviceprovider.ServiceProvider;
import com.google.common.collect.Maps;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public class ServiceProviderHealthcheckTest {

    private TestingCluster testingCluster;
    private ObjectMapper objectMapper;
    private Map<String, TestServiceProvider> serviceProviders = Maps.newHashMap();

    @Before
    public void startTestCluster() throws Exception {
        objectMapper = new ObjectMapper();
        testingCluster = new TestingCluster(3);
        testingCluster.start();
        registerService("localhost-1", 9000, 1);
        //registerService("localhost-2", 9000, 1);
        registerService("localhost-3", 9000, 2);
    }

    @After
    public void stopTestCluster() throws Exception {
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
        CuratorSourceConfig curatorSourceConfig = new CuratorSourceConfig(testingCluster.getConnectString(), "test");
        SimpleShardedServiceFinder<TestShardInfo> serviceFinder = ServiceFinderBuilders.<TestShardInfo>shardedFinderBuilder()
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
                .witHhealthcheckRefreshTimeMillis(10)
                .build();
        serviceFinder.start();
        ServiceNode<TestShardInfo> node = serviceFinder.get(new TestShardInfo(1));
        Assert.assertNotNull(node);
        Assert.assertEquals("localhost-1", node.getHost());
        TestServiceProvider testServiceProvider = serviceProviders.get(node.getHost());
        testServiceProvider.oor();
        Thread.sleep(1000);
        Assert.assertNull(serviceFinder.get(new TestShardInfo(1)));
        serviceFinder.stop();
    }

    private static final class CustomHealthcheck implements Healthcheck {
        private HealthcheckStatus status = HealthcheckStatus.healthy;

        public void setStatus(HealthcheckStatus status) {
            this.status = status;
        }

        @Override
        public HealthcheckStatus check() {
            return status;
        }

    }

    private static final class TestServiceProvider {
        private CustomHealthcheck healthcheck = new CustomHealthcheck();
        private final ObjectMapper objectMapper;
        private final String connectionString;
        private final String host;
        private final int port;
        private final int shardId;

        public TestServiceProvider(ObjectMapper objectMapper,
                                   String connectionString,
                                   String host,
                                   int port,
                                   int shardId) {
            this.objectMapper = objectMapper;
            this.connectionString = connectionString;
            this.host = host;
            this.port = port;
            this.shardId = shardId;
        }

        public void bir() {
            healthcheck.setStatus(HealthcheckStatus.healthy);
        }

        public void oor() {
            healthcheck.setStatus(HealthcheckStatus.unhealthy);
        }

        public void start() throws Exception {
            final ServiceProvider<TestShardInfo> serviceProvider = ServiceProviderBuilders.<TestShardInfo>shardedServiceProviderBuilder()
                    .withConnectionString(connectionString)
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
                    .withHealthcheck(healthcheck)
                    .withRefreshIntervalMillis(10)
                    .buildServiceDiscovery();
            serviceProvider.start();
        }
    }

    private void registerService(String host, int port, int shardId) throws Exception {
        TestServiceProvider serviceProvider = new TestServiceProvider(objectMapper, testingCluster.getConnectString(), host, port, shardId);
        serviceProvider.start();
        serviceProviders.put(host, serviceProvider);
    }

}