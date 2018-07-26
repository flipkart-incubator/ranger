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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.ServiceFinderBuilders;
import com.flipkart.ranger.finder.CuratorSourceConfig;
import com.flipkart.ranger.finder.RoundRobinServiceNodeSelector;
import com.flipkart.ranger.finder.sharded.SimpleShardedServiceFinder;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ServiceNoProviderTest {

    private TestingCluster testingCluster;
    private ObjectMapper objectMapper;

    @Before
    public void startTestCluster() throws Exception {
        objectMapper = new ObjectMapper();
        testingCluster = new TestingCluster(3);
        testingCluster.start();
        //registerService("localhost-1", 9000, 1);
        //registerService("localhost-2", 9000, 1);
        //registerService("localhost-3", 9000, 2);
    }

    @After
    public void stopTestCluster() throws Exception {
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
                .build();
        serviceFinder.start();
        ServiceNode<TestShardInfo> node = serviceFinder.get(new TestShardInfo(1));
        Assert.assertNull(node);
        serviceFinder.stop();

    }

    @Test
    public void testBasicDiscoveryRR() throws Exception {
        CuratorSourceConfig curatorSourceConfig = new CuratorSourceConfig(testingCluster.getConnectString(), "test");
        SimpleShardedServiceFinder<TestShardInfo> serviceFinder = ServiceFinderBuilders.<TestShardInfo>shardedFinderBuilder()
                                                                        .withCuratorSourceConfig(curatorSourceConfig)
                                                                        .withServiceName("test-service")
                                                                        .withNodeSelector(new RoundRobinServiceNodeSelector<TestShardInfo>())
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
        ServiceNode<TestShardInfo> node = serviceFinder.get(new TestShardInfo(1));
        Assert.assertNull(node);
        serviceFinder.stop();
    }

}