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

import com.flipkart.ranger.client.utils.RangerHubTestUtils;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.utils.RangerTestUtils;
import com.flipkart.ranger.core.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

@Slf4j
public class AbstractRangerHubClientTest {

    private static final Service service = RangerTestUtils.getService("test-ns", "test-s");

    @Test
    public void testAbstractHubClient() {
        val testAbstractHub = RangerHubTestUtils.getTestHub();
        testAbstractHub.start();
        TestUtils.sleepForSeconds(3);
        var node = testAbstractHub.getNode(service).orElse(null);
        Assert.assertNotNull(node);
        Assert.assertTrue(node.getHost().equalsIgnoreCase("localhost"));
        Assert.assertEquals(9200, node.getPort());
        Assert.assertEquals(1, node.getNodeData().getShardId());
        Assert.assertFalse(testAbstractHub.getNode(RangerTestUtils.getService("test", "test")).isPresent());
        Assert.assertFalse(testAbstractHub.getNode(service, nodeData -> nodeData.getShardId() == 2).isPresent());
        Assert.assertFalse(testAbstractHub.getNode(RangerTestUtils.getService("test", "test"), nodeData -> nodeData.getShardId() == 1).isPresent());
        testAbstractHub.stop();
    }
}
