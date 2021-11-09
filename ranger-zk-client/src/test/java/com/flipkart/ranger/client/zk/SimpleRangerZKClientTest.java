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

import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class SimpleRangerZKClientTest extends BaseRangerZKClientTest {

    @Test
    public void testBaseClient(){
        val client  = SimpleRangerZKClient.<TestNodeData, Criteria<TestNodeData>>fromCurator()
                .curatorFramework(getCuratorFramework())
                .deserializer(this::read)
                .namespace("test-n")
                .serviceName("s1")
                .disableWatchers(true)
                .mapper(getObjectMapper())
                .build();
        client.start();

        Optional<ServiceNode<TestNodeData>> node = client.getNode();
        Assert.assertTrue(node.isPresent());

        node = client.getNode(c -> c.getNodeId() == 1);
        Assert.assertTrue(node.isPresent());

        node = client.getNode(c -> c.getNodeId() == 2);
        Assert.assertFalse(node.isPresent());
    }
}
