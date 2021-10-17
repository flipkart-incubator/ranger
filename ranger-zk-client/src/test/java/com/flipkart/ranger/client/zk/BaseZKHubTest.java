package com.flipkart.ranger.client.zk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.TestUtils;
import com.flipkart.ranger.core.healthcheck.HealthcheckResult;
import com.flipkart.ranger.core.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.signals.ExternalTriggeredSignal;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.util.Exceptions;
import com.flipkart.ranger.zookeeper.ServiceProviderBuilders;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Getter
public abstract class BaseZKHubTest extends BaseZKTest{
    @Data
    @Builder
    @AllArgsConstructor
    private static class TestCriteria implements Criteria<TestNodeData> {
        @Override
        public boolean apply(TestNodeData nodeData) {
            return nodeData.getNodeId() == 1;
        }
    }

    protected abstract AbstractZKHubClient getClient();

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
