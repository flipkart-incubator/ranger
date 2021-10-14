/**
 * Copyright 2016 Flipkart Internet Pvt. Ltd.
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

package com.flipkart.ranger.zookeeper.healthservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.TestUtils;
import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinder;
import com.flipkart.ranger.core.healthcheck.Healthchecks;
import com.flipkart.ranger.core.healthservice.TimeEntity;
import com.flipkart.ranger.core.healthservice.monitor.sample.RotationStatusMonitor;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.FilterCriteria;
import com.flipkart.ranger.core.util.Exceptions;
import com.flipkart.ranger.zookeeper.ServiceFinderBuilders;
import com.flipkart.ranger.zookeeper.ServiceProviderBuilders;
import lombok.val;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ServiceProviderIntegrationTest {

    final String filePath = "/tmp/rangerRotationFile.html";
    File file = new File(filePath);
    final String filePath2 = "/tmp/rangerRotationFile2.html";
    File anotherFile = new File(filePath2);

    private TestingCluster testingCluster;
    private ObjectMapper objectMapper;

    SimpleUnshardedServiceFinder serviceFinder;

    private static final class UnshardedClusterInfo {
        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }
    @Before
    public void startTestCluster() throws Exception {
        objectMapper = new ObjectMapper();
        testingCluster = new TestingCluster(3);
        testingCluster.start();

        /* registering 3 with RotationMonitor on file and 1 on anotherFile */
        registerService("localhost-1", 9000, 1, file);
        registerService("localhost-2", 9000, 1, file);
        registerService("localhost-3", 9000, 2, file);

        registerService("localhost-4", 9000, 2, anotherFile);

        serviceFinder = ServiceFinderBuilders.<UnshardedClusterInfo>unshardedFinderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withDeserializer(data -> {
                    try {
                        return objectMapper.readValue(data, new TypeReference<ServiceNode<UnshardedClusterInfo>>() {});
                    } catch (IOException e) {
                        Exceptions.illegalState(e);
                    }
                    return null;
                })
                .build();
        serviceFinder.start();
    }

    @After
    public void stopTestCluster() throws Exception {
        if (null != testingCluster) {
            testingCluster.close();
        }
        serviceFinder.stop();
        TestUtils.sleepForSeconds(1);
    }

    @Test
    public void testBasicDiscovery() throws Exception {

        /* clean slate */
        boolean delete = file.delete();
        delete = anotherFile.delete();

        /* with file existing, 3 nodes should be healthy */
        boolean filecreate = file.createNewFile();
        System.out.println("created file");
        TestUtils.sleepForSeconds(8);
        List<ServiceNode<UnshardedClusterInfo>> all = serviceFinder.getAll((FilterCriteria) nodeData -> true);
        System.out.println("all = " + all);
        Assert.assertEquals(3, all.size());

        /* with file deleted, all 3 nodes should be unhealthy */
        delete = file.delete();
        System.out.println("deleted file");
        TestUtils.sleepForSeconds(8);
        all = serviceFinder.getAll(new FilterCriteria() {
            @Override
            public boolean apply(Object nodeData) {
                return true;
            }
        });
        System.out.println("all = " + all);
        Assert.assertEquals(0, all.size());

        /* with anotherFile created, the 4th node should become healthy and discoverable */
        filecreate = anotherFile.createNewFile();
        System.out.println("created anotherFile");
        TestUtils.sleepForSeconds(6);
        all = serviceFinder.getAll(new FilterCriteria() {
            @Override
            public boolean apply(Object nodeData) {
                return true;
            }
        });
        System.out.println("all = " + all);
        Assert.assertEquals(1, all.size());

        /* clean slate */
        delete = file.delete();
        delete = anotherFile.delete();
    }

    private void registerService(String host, int port, int shardId, File file) throws Exception {
        val serviceProvider = ServiceProviderBuilders.unshardedServiceProviderBuilder()
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
                .withHealthcheck(Healthchecks.defaultHealthyCheck())
                .withIsolatedHealthMonitor(new RotationStatusMonitor(TimeEntity.everySecond(), file.getAbsolutePath()))
                .build();
        serviceProvider.start();
    }
}
