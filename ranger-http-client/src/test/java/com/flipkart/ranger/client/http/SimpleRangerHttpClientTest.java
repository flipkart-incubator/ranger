package com.flipkart.ranger.client.http;

import com.flipkart.ranger.core.TestUtils;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.http.serde.HTTPResponseDataDeserializer;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class SimpleRangerHttpClientTest extends BaseRangerHttpClientTest{

    @Test
    public void testSimpleHttpRangerClient(){
        val client = SimpleRangerHttpClient.<TestNodeData, Criteria<TestNodeData>, HTTPResponseDataDeserializer<TestNodeData>>builder()
                .clientConfig(getHttpClientConfig())
                .mapper(getObjectMapper())
                .deserializer(this::read)
                .namespace("test-n")
                .serviceName("test-s")
                .refreshTimeMs(5000)
                .build();
        client.start();

        TestUtils.sleepForSeconds(6);

        Optional<ServiceNode<TestNodeData>> node = client.getNode();
        Assert.assertTrue(node.isPresent());

        node = client.getNode(nodeData -> nodeData.getNodeId() == 1);
        Assert.assertTrue(node.isPresent());

        node = client.getNode(nodeData -> nodeData.getNodeId() == 2);
        Assert.assertFalse(node.isPresent());

        client.stop();
    }
}
