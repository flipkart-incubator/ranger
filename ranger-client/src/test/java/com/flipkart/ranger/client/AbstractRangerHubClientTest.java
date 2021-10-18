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
package com.flipkart.ranger.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.client.stubs.RangerTestHub;
import com.flipkart.ranger.client.stubs.TestShardInfo;
import com.flipkart.ranger.client.utils.RangerHubTestUtils;
import com.flipkart.ranger.core.TestUtils;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

@Slf4j
public class AbstractRangerHubClientTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Service service = new Service("test-ns", "test-s");

    @Test
    public void testAbstractHubClient() {
        RangerTestHub testAbstractHub = RangerHubTestUtils.getTestHub();
        testAbstractHub.start();
        TestUtils.sleepForSeconds(3);
        Optional<ServiceNode<TestShardInfo>> node = testAbstractHub.getNode(service);
        Assert.assertTrue(node.isPresent());
        Assert.assertTrue(node.get().getHost().equalsIgnoreCase("localhost"));
        Assert.assertEquals(9200, node.get().getPort());
        Assert.assertEquals(1, node.get().getNodeData().getShardId());

        node = testAbstractHub.getNode(new Service("test", "test"));
        Assert.assertFalse(node.isPresent());

        node = testAbstractHub.getNode(service, nodeData -> nodeData.getShardId() == 2);
        Assert.assertFalse(node.isPresent());

        node = testAbstractHub.getNode(new Service("test", "test"), nodeData -> nodeData.getShardId() == 1);
        Assert.assertFalse(node.isPresent());

        testAbstractHub.start();
    }
}
