/*
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
package com.flipkart.ranger.client.zk;

import com.flipkart.ranger.core.TestUtils;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

@Slf4j
public class UnshardedZKRangerClientTest extends BaseRangerZKClientTest {

    @Test
    public void testShardedHub(){
        val zkHubClient =UnshardedRangerZKHubClient.<TestNodeData, Criteria<TestNodeData>>builder()
                .namespace("test-n")
                .connectionString(getTestingCluster().getConnectString())
                .curatorFramework(getCuratorFramework())
                .disablePushUpdaters(true)
                .mapper(getObjectMapper())
                .deserializer(this::read)
                .build();

        zkHubClient.start();
        TestUtils.sleepForSeconds(6);

        val service = new Service("test-n", "s1");
        Optional<ServiceNode<TestNodeData>> node = zkHubClient.getNode(new Service("test-n", "s1"));
        Assert.assertTrue(node.isPresent());

        node = zkHubClient.getNode(service, nodeData -> nodeData.getNodeId() == 1);
        Assert.assertTrue(node.isPresent());

        zkHubClient.stop();
    }
}
