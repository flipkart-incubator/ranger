package com.flipkart.ranger.client.zk;

import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.units.TestNodeData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShardedZKHubTest extends BaseZKHubTest {

    @Override
    protected AbstractZKHubClient getClient() {
        return ShardedZKHubClient.<TestNodeData, Criteria<TestNodeData>>builder()
                .namespace("test-n")
                .connectionString(getTestingCluster().getConnectString())
                .curatorFramework(getCuratorFramework())
                .disablePushUpdaters(true)
                .mapper(getObjectMapper())
                .deserializer(this::read)
                .build();
    }
}
