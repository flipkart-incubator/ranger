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
package com.flipkart.ranger.http.model;

import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.http.ResourceHelper;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

public class ServiceNodeResponseTest {

    @Value
    @Builder
    @Jacksonized
    static class TestNodeInfo{
        int shardId;
        String region;
    }

    @Test
    public void testServiceNodesResponse(){
        val serviceNodesResponse = ResourceHelper.getResource("fixtures/serviceNodesResponse.json", ServiceNodesResponse.class);
        Assert.assertNotNull(serviceNodesResponse);
        Assert.assertFalse(serviceNodesResponse.getData().isEmpty());
        Assert.assertNotNull(((ServiceNode<?>) serviceNodesResponse.getData().get(0)).getNodeData());
        Assert.assertNotNull(((ServiceNode<?>) serviceNodesResponse.getData().get(1)).getNodeData());
        Assert.assertEquals(((ServiceNode<?>) serviceNodesResponse.getData().get(0)).getHost(), "localhost-1");
        Assert.assertEquals(((ServiceNode<?>) serviceNodesResponse.getData().get(1)).getHost(), "localhost-2");
    }
}
