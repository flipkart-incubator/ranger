package com.flipkart.ranger.client.zk;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ShardedZKHubClientTest {

    @Data
    @Builder
    @AllArgsConstructor
    private static class TestCriteria implements Criteria<TestNodeData> {
        @Override
        public boolean apply(TestNodeData nodeData) {
            return nodeData.getNodeId() == 1;
        }
    }

    private TestingCluster testingCluster;
    private ObjectMapper objectMapper = new ObjectMapper();
    private CuratorFramework curatorFramework;

    @Before
    public void startTestCluster() throws Exception {
        objectMapper = new ObjectMapper();
        testingCluster = new TestingCluster(3);
        testingCluster.start();
        curatorFramework = CuratorFrameworkFactory.builder()
                .namespace("test-n")
                .connectString(testingCluster.getConnectString())
                .retryPolicy(new ExponentialBackoffRetry(1000, 100))
                .build();
        curatorFramework.start();
        curatorFramework.blockUntilConnected();
        log.debug("Started zk subsystem");
    }

    @After
    public void stopTestCluster() throws Exception {
        log.debug("Stopping zk subsystem");
        curatorFramework.close();
        if (null != testingCluster) {
            testingCluster.close();
        }
    }

    private ServiceNode<TestNodeData> read(final byte[] data) {
        try {
            return objectMapper.readValue(data, new TypeReference<ServiceNode<TestNodeData>>() {});
        }
        catch (IOException e) {
            Exceptions.illegalState(e);
        }
        return null;
    }

    private byte[] write(final ServiceNode<TestNodeData> node) {
        try {
            return objectMapper.writeValueAsBytes(node);
        }
        catch (IOException e) {
            Exceptions.illegalState(e);
        }
        return null;
    }

    @Test
    public void testShardedHub(){
        val zkHubClient = ShardedZKHubClient.<TestNodeData, Criteria<TestNodeData>>builder()
                .namespace("test-n")
                .connectionString(testingCluster.getConnectString())
                .curatorFramework(curatorFramework)
                .disablePushUpdaters(true)
                .mapper(objectMapper)
                .deserializer(this::read)
                .build();
        zkHubClient.start();

        ExternalTriggeredSignal<HealthcheckResult> refreshProviderSignal = new ExternalTriggeredSignal<>(
                () -> HealthcheckResult.builder()
                        .status(HealthcheckStatus.healthy)
                        .updatedTime(new Date().getTime())
                        .build(), Collections.emptyList());
        val provider1 = ServiceProviderBuilders.<TestNodeData>shardedServiceProviderBuilder()
                .withHostname("localhost")
                .withPort(1080)
                .withNamespace("test-n")
                .withServiceName("s1")
                .withSerializer(this::write)
                .withNodeData(new TestNodeData(1))
                .withHealthcheck(() -> HealthcheckStatus.healthy)
                .withadditionalRefreshSignal(refreshProviderSignal)
                .withCuratorFramework(curatorFramework)
                .build();
        provider1.start();

        refreshProviderSignal.trigger();

        TestUtils.sleepForSeconds(6);

        val service = new Service("test-n", "s1");
        Optional<ServiceNode<TestNodeData>> node = zkHubClient.getNode(new Service("test-n", "s1"));
        Assert.assertTrue(node.isPresent());

        node = zkHubClient.getNode(service, nodeData -> nodeData.getNodeId() == 1);
        Assert.assertTrue(node.isPresent());

        node = zkHubClient.getNode(service, nodeData -> nodeData.getNodeId() == 2);
        Assert.assertFalse(node.isPresent());

        zkHubClient.stop();
        provider1.stop();
    }
}
