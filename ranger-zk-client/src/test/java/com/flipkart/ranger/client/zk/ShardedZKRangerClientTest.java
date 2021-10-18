package com.flipkart.ranger.client.zk;

import com.flipkart.ranger.core.TestUtils;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

@Slf4j
public class ShardedZKRangerClientTest extends BaseRangerZKClientTest {

    @Test
    public void testShardedHub(){
        val zkHubClient = ShardedRangerZKHubClient.<TestNodeData, Criteria<TestNodeData>>builder()
                .namespace("test-n")
                .connectionString(getTestingCluster().getConnectString())
                .curatorFramework(getCuratorFramework())
                .disablePushUpdaters(true)
                .mapper(getObjectMapper())
                .deserializer(this::read)
                .build();

        zkHubClient.start();
        TestUtils.sleepForSeconds(6);

        val service = new Service("test-n", "s1");
        Optional<ServiceNode<TestNodeData>> node = zkHubClient.getNode(new Service("test-n", "s1"));
        Assert.assertTrue(node.isPresent());

        node = zkHubClient.getNode(service, nodeData -> nodeData.getNodeId() == 1);
        Assert.assertTrue(node.isPresent());

        zkHubClient.stop();
    }
}
