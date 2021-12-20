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
import com.flipkart.ranger.core.healthcheck.Healthcheck;
import com.flipkart.ranger.core.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.utils.RangerTestUtils;
import com.flipkart.ranger.core.utils.TestUtils;
import com.flipkart.ranger.zookeeper.ServiceFinderBuilders;
import com.flipkart.ranger.zookeeper.ServiceProviderBuilders;
import com.google.common.collect.Maps;
import lombok.val;
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

    @Test
    public void testBasicDiscovery() {
        val serviceFinder = ServiceFinderBuilders.<TestNodeData>shardedFinderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
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
                .withNodeRefreshIntervalMs(1000)
                .build();
        serviceFinder.start();
        val node = serviceFinder.get(RangerTestUtils.getCriteria(1));
        Assert.assertTrue(node.isPresent());
        Assert.assertEquals("localhost-1", node.get().getHost());
        TestServiceProvider testServiceProvider = serviceProviders.get(node.get().getHost());
        testServiceProvider.oor();
        TestUtils.sleepForSeconds(6);
        Assert.assertFalse(serviceFinder.get(RangerTestUtils.getCriteria(1)).isPresent());
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
            val serviceProvider = ServiceProviderBuilders.<TestNodeData>shardedServiceProviderBuilder()
                    .withConnectionString(connectionString)
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
                    .withHealthcheck(healthcheck)
                    .withHealthUpdateIntervalMs(1000)
                    .build();
            serviceProvider.start();
        }
    }

    private void registerService(String host, int port, int shardId) throws Exception {
        val serviceProvider = new TestServiceProvider(objectMapper, testingCluster.getConnectString(), host, port, shardId);
        serviceProvider.start();
        serviceProviders.put(host, serviceProvider);
    }

}