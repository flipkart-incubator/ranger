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
package com.flipkart.ranger.http.server.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.client.http.UnshardedRangerHttpHubClient;
import com.flipkart.ranger.common.server.ShardInfo;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.model.ServiceNodesResponse;
import com.flipkart.ranger.http.server.config.RangerHttpConfiguration;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class RangerHttpServerUtils {

    private RangerHttpServerUtils(){}

    public static void verifyPreconditions(RangerHttpConfiguration rangerConfiguration){
        Preconditions.checkNotNull(rangerConfiguration,
                "ranger configuration can't be null");
        Preconditions.checkNotNull(rangerConfiguration.getNamespace(),
                "Namespace can't be null");
        Preconditions.checkArgument(null != rangerConfiguration.getHttpClientConfigs() && !rangerConfiguration.getHttpClientConfigs().isEmpty(),
                "Http client config can't be null");
    }

    public static UnshardedRangerHttpHubClient<ShardInfo, Criteria<ShardInfo>> buildRangerHub(
            String namespace,
            int nodeRefreshTimeMs,
            HttpClientConfig clientConfig,
            ObjectMapper mapper
    ){
        return UnshardedRangerHttpHubClient.<ShardInfo, Criteria<ShardInfo>>builder()
                .namespace(namespace)
                .mapper(mapper)
                .clientConfig(clientConfig)
                .nodeRefreshIntervalMs(nodeRefreshTimeMs)
                .deserializer(data -> {
                    try {
                        mapper.readValue(data, new TypeReference<ServiceNodesResponse<ShardInfo>>() {
                        });
                    } catch (IOException e) {
                        log.warn("Error parsing node data with value {}", new String(data));
                    }
                    return null;
                })
                .build();
    }
}
