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
import com.flipkart.ranger.finder.unsharded.UnshardedClusterFinder;
import com.flipkart.ranger.finder.unsharded.UnshardedClusterInfo;
import com.flipkart.ranger.healthcheck.Healthchecks;
import com.flipkart.ranger.serviceprovider.ServiceProvider;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class SimpleServiceProviderTest {

    private TestingCluster testingCluster;
    private ObjectMapper objectMapper;

    @Before
    public void startTestCluster() throws Exception {
        objectMapper = new ObjectMapper();
        testingCluster = new TestingCluster(3);
        testingCluster.start();
        registerService("localhost-1", 9000, 1);
        registerService("localhost-2", 9000, 1);
        registerService("localhost-3", 9000, 2);
    }

    @After
    public void stopTestCluster() throws Exception {
        if(null != testingCluster) {
            testingCluster.close();
        }
    }

    @Test
    public void testBasicDiscovery() throws Exception {
        CuratorSourceConfig curatorSourceConfig = new CuratorSourceConfig(testingCluster.getConnectString(), "test");
        UnshardedClusterFinder serviceFinder = ServiceFinderBuilders.unshardedFinderBuilder()
                .withCuratorSourceConfig(curatorSourceConfig)
                .withServiceName("test-service")
                .withDeserializer(new Deserializer<UnshardedClusterInfo>() {
                    @Override
                    public ServiceNode<UnshardedClusterInfo> deserialize(byte[] data) {
                        try {
                            return objectMapper.readValue(data,
                                    new TypeReference<ServiceNode<UnshardedClusterInfo>>() {
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
            ServiceNode node = serviceFinder.get(null);
            Assert.assertNotNull(node);
            System.out.println(node.getHost());
        }
        Multiset<String> frequency = HashMultiset.create();
        long startTime = System.currentTimeMillis();
        for(long i = 0; i <1000000; i++)
        {
            ServiceNode node = serviceFinder.get(null);
            Assert.assertNotNull(node);
            frequency.add(node.getHost());
        }
        System.out.println("1 Million lookups and freq counting took (ms):" + (System.currentTimeMillis() -startTime));
        System.out.println("Frequency: " + frequency);
        //while (true);
    }

    private void registerService(String host, int port, int shardId) throws Exception {
        ServiceProvider<UnshardedClusterInfo> serviceProvider = ServiceProviderBuilders.unshardedServiceProviderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withSerializer(new Serializer<UnshardedClusterInfo>() {
                    @Override
                    public byte[] serialize(ServiceNode<UnshardedClusterInfo> data) {
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
                .withHealthcheck(Healthchecks.defaultHealthyCheck())
                .buildServiceDiscovery();
        serviceProvider.start();
    }
}