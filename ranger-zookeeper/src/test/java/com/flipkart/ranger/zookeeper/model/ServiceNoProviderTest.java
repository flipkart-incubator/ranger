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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.finder.SimpleShardedServiceFinder;
import com.flipkart.ranger.core.finder.nodeselector.RoundRobinServiceNodeSelector;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.utils.RangerTestUtils;
import com.flipkart.ranger.zookeeper.ServiceFinderBuilders;
import lombok.val;
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
    }

    @After
    public void stopTestCluster() throws Exception {
        if (null != testingCluster) {
            testingCluster.close();
        }
    }

    @Test
    public void testBasicDiscovery() {
        SimpleShardedServiceFinder<TestNodeData> serviceFinder = ServiceFinderBuilders.<TestNodeData>shardedFinderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withDeserializer(data -> {
                    try {
                        return objectMapper.readValue(data,
                                                      new TypeReference<ServiceNode<TestNodeData>>() {
                                                      });
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .build();
        serviceFinder.start();
        val node = serviceFinder.get(RangerTestUtils.getCriteria(1)).orElse(null);
        Assert.assertNull(node);
        serviceFinder.stop();

    }

    @Test
    public void testBasicDiscoveryRR() {
        val serviceFinder = ServiceFinderBuilders.<TestNodeData>shardedFinderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withNodeSelector(new RoundRobinServiceNodeSelector<>())
                .withDeserializer(data -> {
                    try {
                        return objectMapper.readValue(data,
                                                      new TypeReference<ServiceNode<TestNodeData>>() {
                                                      });
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .build();
        serviceFinder.start();
        val node = serviceFinder.get(RangerTestUtils.getCriteria(1)).orElse(null);
        Assert.assertNull(node);
        serviceFinder.stop();
    }

}