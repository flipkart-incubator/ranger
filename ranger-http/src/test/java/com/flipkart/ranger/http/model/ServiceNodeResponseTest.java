package com.flipkart.ranger.http.model;

import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.http.ResourceHelper;
import lombok.*;
import org.junit.Assert;
import org.junit.Test;

public class ServiceNodeResponseTest {

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    static class TestNodeInfo{
        private int shardId;
        private String farmId;
    }

    @Test
    public void testServiceNodesResponse(){
        val serviceNodesResponse = ResourceHelper.getResource("fixtures/serviceNodesResponse.json", ServiceNodesResponse.class);
        Assert.assertNotNull(serviceNodesResponse);
        Assert.assertFalse(serviceNodesResponse.getNodes().isEmpty());
        Assert.assertNotNull(((ServiceNode<?>) serviceNodesResponse.getNodes().get(0)).getNodeData());
        Assert.assertNotNull(((ServiceNode<?>) serviceNodesResponse.getNodes().get(1)).getNodeData());
        Assert.assertEquals(((ServiceNode<?>) serviceNodesResponse.getNodes().get(0)).getHost(), "localhost-1");
        Assert.assertEquals(((ServiceNode<?>) serviceNodesResponse.getNodes().get(1)).getHost(), "localhost-2");
    }
}
