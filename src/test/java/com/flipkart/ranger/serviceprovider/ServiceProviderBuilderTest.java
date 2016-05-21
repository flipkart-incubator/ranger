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

package com.flipkart.ranger.serviceprovider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.ServiceProviderBuilders;
import com.flipkart.ranger.finder.unsharded.UnshardedClusterInfo;
import com.flipkart.ranger.healthcheck.Healthchecks;
import com.flipkart.ranger.model.Serializer;
import com.flipkart.ranger.model.ServiceNode;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author tushar.naik
 * @version 1.0
 * @date 12/03/16 - 7:40 PM
 */
public class ServiceProviderBuilderTest {

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
        if(null != testingCluster) {
            testingCluster.close();
        }
    }

    @Test
    public void testbuilder() throws Exception {
        final String host = "localhost";
        final int port = 9000;
        Exception exception = null;
        try {
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
                    .buildServiceDiscovery();
        } catch (Exception e) {
            exception = e;
        }
        Assert.assertTrue(exception instanceof IllegalArgumentException);

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
                .withHealthcheck(Healthchecks.defaultHealthyCheck())
                .withPort(port)
                .buildServiceDiscovery();
    }
}