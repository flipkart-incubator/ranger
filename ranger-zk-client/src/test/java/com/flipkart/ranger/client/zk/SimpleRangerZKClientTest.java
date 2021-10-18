package com.flipkart.ranger.client.zk;

import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataDeserializer;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class SimpleRangerZKClientTest extends BaseRangerZKClientTest {

    @Test
    public void testBaseClient(){
        val client  = SimpleRangerZKClient.<TestNodeData, Criteria<TestNodeData>, ZkNodeDataDeserializer<TestNodeData>>fromCurator()
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
