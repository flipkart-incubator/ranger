package com.flipkart.ranger.client.zk;

import com.flipkart.ranger.core.TestUtils;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

@Slf4j
@Getter
public abstract class AbstractZKRangerClientTest extends BaseRangerZKClientTest {
    @Data
    @Builder
    @AllArgsConstructor
    private static class TestCriteria implements Criteria<TestNodeData> {
        @Override
        public boolean apply(TestNodeData nodeData) {
            return nodeData.getNodeId() == 1;
        }
    }

    protected abstract AbstractRangerZKHubClient getClient();

    @Test
    public void testShardedHub(){
        val zkHubClient = getClient();
        zkHubClient.start();
        TestUtils.sleepForSeconds(6);

        val service = new Service("test-n", "s1");
        Optional<ServiceNode<TestNodeData>> node = zkHubClient.getNode(new Service("test-n", "s1"));
        Assert.assertTrue(node.isPresent());

        node = zkHubClient.getNode(service, new TestCriteria());
        Assert.assertTrue(node.isPresent());

        zkHubClient.stop();
    }
}
