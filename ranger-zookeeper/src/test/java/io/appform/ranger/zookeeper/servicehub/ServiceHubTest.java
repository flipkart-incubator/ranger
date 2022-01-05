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
package io.appform.ranger.zookeeper.servicehub;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.appform.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import io.appform.ranger.core.healthcheck.HealthcheckResult;
import io.appform.ranger.core.healthcheck.HealthcheckStatus;
import io.appform.ranger.core.model.ServiceNode;
import io.appform.ranger.core.signals.ExternalTriggeredSignal;
import io.appform.ranger.core.units.TestNodeData;
import io.appform.ranger.core.util.Exceptions;
import io.appform.ranger.core.utils.RangerTestUtils;
import io.appform.ranger.zookeeper.ServiceProviderBuilders;
import io.appform.ranger.zookeeper.servicefinderhub.ZkServiceDataSource;
import io.appform.ranger.zookeeper.servicefinderhub.ZkServiceFinderHubBuilder;
import io.appform.ranger.zookeeper.servicefinderhub.ZkShardedServiceFinderFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

/**
 *
 */
@Slf4j
public class ServiceHubTest {

    private static final String NAMESPACE = "test";

    private TestingCluster testingCluster;
    private ObjectMapper objectMapper = new ObjectMapper();
    private CuratorFramework curatorFramework;

    @Before
    public void startTestCluster() throws Exception {
        objectMapper = new ObjectMapper();
        testingCluster = new TestingCluster(3);
        testingCluster.start();
        curatorFramework = CuratorFrameworkFactory.builder()
                .namespace(NAMESPACE)
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

    @Test
    public void testHub() {
        ExternalTriggeredSignal<Void> refreshHubSignal = new ExternalTriggeredSignal<>(() -> null, Collections.emptyList());
        val hub = new ZkServiceFinderHubBuilder<TestNodeData, MapBasedServiceRegistry<TestNodeData>>()
                .withCuratorFramework(curatorFramework)
                .withNamespace("test")
                .withRefreshFrequencyMs(1000)
                .withServiceDataSource(new ZkServiceDataSource("test", testingCluster.getConnectString(), curatorFramework))
                .withServiceFinderFactory(ZkShardedServiceFinderFactory.<TestNodeData>builder()
                                                  .curatorFramework(curatorFramework)
                                                  .deserializer(this::read)
                                                  .build())
                .withExtraRefreshSignal(refreshHubSignal)
                .build();
        hub.start();

        val refreshProviderSignal = new ExternalTriggeredSignal<>(
                () -> HealthcheckResult.builder()
                        .status(HealthcheckStatus.healthy)
                        .updatedTime(new Date().getTime())
                        .build(), Collections.emptyList());
        val provider1 = ServiceProviderBuilders.<TestNodeData>shardedServiceProviderBuilder()
                .withHostname("localhost")
                .withPort(1080)
                .withNamespace(NAMESPACE)
                .withServiceName("s1")
                .withSerializer(this::write)
                .withNodeData(TestNodeData.builder().shardId(1).build())
                .withHealthcheck(() -> HealthcheckStatus.healthy)
                .withAdditionalRefreshSignal(refreshProviderSignal)
                .withCuratorFramework(curatorFramework)
                .build();
        provider1.start();

        refreshProviderSignal.trigger();
        refreshHubSignal.trigger();
        RangerTestUtils.sleepUntilHubStarts(hub);

        val node = hub.finder(RangerTestUtils.getService(NAMESPACE, "s1"))
                .flatMap(finder -> finder.get(nodeData -> nodeData.getShardId() == 1)).orElse(null);
        Assert.assertNotNull(node);
        hub.stop();
        provider1.stop();
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
}
