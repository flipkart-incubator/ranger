package com.flipkart.ranger.client.http;

import com.flipkart.ranger.core.TestUtils;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class UnshardedRangerHttpClientTest extends BaseRangerHttpClientTest {

    @Test
    public void testUnshardedRangerHubClient(){
        val client = UnshardedRangerHttpHubClient.<TestNodeData, Criteria<TestNodeData>>builder()
                .clientConfig(getHttpClientConfig())
                .namespace("test-n")
                .deserializer(this::read)
                .mapper(getObjectMapper())
                .build();
        client.start();

        TestUtils.sleepForSeconds(6);

        val service = new Service("test-n", "test-s");
        Optional<ServiceNode<TestNodeData>> node = client.getNode(service);
        Assert.assertTrue(node.isPresent());

        node = client.getNode(service, nodeData -> nodeData.getNodeId() == 1);
        Assert.assertTrue(node.isPresent());

        node = client.getNode(service, nodeData -> nodeData.getNodeId() == 2);
        Assert.assertFalse(node.isPresent());

        client.stop();
    }
}
