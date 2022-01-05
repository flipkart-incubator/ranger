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

import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.utils.RangerTestUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

@Slf4j
public class UnshardedZKRangerClientTest extends BaseRangerZKClientTest {

    @Test
    public void testShardedHub(){
        val zkHubClient =UnshardedRangerZKHubClient.<TestNodeData>builder()
                .namespace("test-n")
                .connectionString(getTestingCluster().getConnectString())
                .curatorFramework(getCuratorFramework())
                .disablePushUpdaters(true)
                .mapper(getObjectMapper())
                .deserializer(this::read)
                .refreshTimeMs(1000)
                .build();
        zkHubClient.start();
        RangerTestUtils.sleepUntilHubStarts(zkHubClient.getHub());
        val service = RangerTestUtils.getService("test-n", "s1");
        Assert.assertNotNull(zkHubClient.getNode(service).orElse(null));
        Assert.assertNotNull(zkHubClient.getNode(service, nodeData -> nodeData.getShardId() == 1).orElse(null));
        zkHubClient.stop();
    }
}
