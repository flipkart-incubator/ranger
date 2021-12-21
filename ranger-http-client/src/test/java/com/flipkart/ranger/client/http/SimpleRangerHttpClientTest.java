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
package com.flipkart.ranger.client.http;

import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.utils.TestUtils;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

public class SimpleRangerHttpClientTest extends BaseRangerHttpClientTest{

    @Test
    public void testSimpleHttpRangerClient(){
        val client = SimpleRangerHttpClient.<TestNodeData>builder()
                .clientConfig(getHttpClientConfig())
                .mapper(getObjectMapper())
                .deserializer(this::read)
                .namespace("test-n")
                .serviceName("test-s")
                .nodeRefreshIntervalMs(5000)
                .build();
        client.start();

        TestUtils.sleepForSeconds(6);

        ServiceNode<TestNodeData> node = client.getNode().orElse(null);
        Assert.assertNotNull(node);

        node = client.getNode(nodeData -> nodeData.getShardId() == 1).orElse(null);
        Assert.assertNotNull(node);

        node = client.getNode(nodeData -> nodeData.getShardId() == 2).orElse(null);
        Assert.assertNull(node);

        client.stop();
    }
}
