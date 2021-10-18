package com.flipkart.ranger.server.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.client.RangerClientConstants;
import com.flipkart.ranger.client.zk.UnshardedRangerZKHubClient;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.server.config.RangerConfiguration;
import com.flipkart.ranger.server.model.ShardInfo;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;

import java.io.IOException;

@Slf4j
public class RangerServerUtils {

    private RangerServerUtils(){}

    public static void verifyPreconditions(RangerConfiguration rangerConfiguration){
        Preconditions.checkNotNull(rangerConfiguration,
                "ranger configuration can't be null");
        Preconditions.checkNotNull(rangerConfiguration.getNamespace(),
                "Namespace can't be null");
        Preconditions.checkNotNull(rangerConfiguration.getZookeeper(),
                "Zookeeper can't be null");
    }

    public static CuratorFramework buildCurator(RangerConfiguration rangerConfiguration){
        return CuratorFrameworkFactory.newClient(
                rangerConfiguration.getZookeeper(),
                new RetryForever(RangerClientConstants.CONNECTION_RETRY_TIME)
        );
    }

    public static UnshardedRangerZKHubClient<ShardInfo, Criteria<ShardInfo>> buildRangerHub(
            CuratorFramework curatorFramework,
            RangerConfiguration rangerConfiguration,
            ObjectMapper mapper
    ){
        return UnshardedRangerZKHubClient.<ShardInfo, Criteria<ShardInfo>>builder()
                .namespace(rangerConfiguration.getNamespace())
                .connectionString(rangerConfiguration.getZookeeper())
                .curatorFramework(curatorFramework)
                .disablePushUpdaters(rangerConfiguration.isDisablePushUpdaters())
                .mapper(mapper)
                .services(rangerConfiguration.getServices())
                .refreshTimeMs(rangerConfiguration.getNodeRefreshTimeMs())
                .deserializer(data -> {
                    try {
                        mapper.readValue(data, new TypeReference<ServiceNode<ShardInfo>>() {
                        });
                    } catch (IOException e) {
                        log.warn("Error parsing node data with value {}", new String(data));
                    }
                    return null;
                })
                .build();
    }
}
