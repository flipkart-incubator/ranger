package com.flipkart.ranger.client.zk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.healthcheck.HealthcheckResult;
import com.flipkart.ranger.core.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.serviceprovider.ServiceProvider;
import com.flipkart.ranger.core.signals.ExternalTriggeredSignal;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.util.Exceptions;
import com.flipkart.ranger.zookeeper.ServiceProviderBuilders;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataSerializer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Getter
public abstract class BaseRangerZKClientTest {

    private TestingCluster testingCluster;
    private ObjectMapper objectMapper = new ObjectMapper();
    private CuratorFramework curatorFramework;
    private ServiceProvider<TestNodeData, ZkNodeDataSerializer<TestNodeData>> provider;

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
        initilizeProvider();
        log.debug("Started zk subsystem");
    }

    @After
    public void stopTestCluster() throws Exception {
        log.debug("Stopping zk subsystem");
        curatorFramework.close();
        if (null != testingCluster) {
            testingCluster.close();
        }
        if(null != provider){
            provider.stop();
        }
    }

    protected ServiceNode<TestNodeData> read(final byte[] data) {
        try {
            return getObjectMapper().readValue(data, new TypeReference<ServiceNode<TestNodeData>>() {});
        }
        catch (IOException e) {
            Exceptions.illegalState(e);
        }
        return null;
    }

    protected byte[] write(final ServiceNode<TestNodeData> node) {
        try {
            return getObjectMapper().writeValueAsBytes(node);
        }
        catch (IOException e) {
            Exceptions.illegalState(e);
        }
        return null;
    }

    protected void initilizeProvider(){
        ExternalTriggeredSignal<HealthcheckResult> refreshProviderSignal = new ExternalTriggeredSignal<>(
                () -> HealthcheckResult.builder()
                        .status(HealthcheckStatus.healthy)
                        .updatedTime(new Date().getTime())
                        .build(), Collections.emptyList());
        provider = ServiceProviderBuilders.<TestNodeData>shardedServiceProviderBuilder()
                .withHostname("localhost")
                .withPort(1080)
                .withNamespace("test-n")
                .withServiceName("s1")
                .withSerializer(this::write)
                .withNodeData(new TestNodeData(1))
                .withHealthcheck(() -> HealthcheckStatus.healthy)
                .withadditionalRefreshSignal(refreshProviderSignal)
                .withCuratorFramework(getCuratorFramework())
                .build();
        provider.start();
        refreshProviderSignal.trigger();
    }


}
