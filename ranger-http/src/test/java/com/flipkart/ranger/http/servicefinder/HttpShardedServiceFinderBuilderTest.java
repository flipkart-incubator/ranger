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
package com.flipkart.ranger.http.servicefinder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.utils.RangerTestUtils;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.model.ServiceNodesResponse;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.Data;
import lombok.val;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 *
 */
public class HttpShardedServiceFinderBuilderTest {

    @Data
    private static final class NodeData {
        private final String name;

        public NodeData(@JsonProperty("name") String name) {
            this.name = name;
        }
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Rule
    public WireMockRule server = new WireMockRule(8888);

    @Test
    public void testFinder() throws Exception {
        val testNode = new NodeData("testNode");
        val node = ServiceNode.<NodeData>builder().host("127.0.0.1").port(80).nodeData(testNode).build();
        node.setHealthcheckStatus(HealthcheckStatus.healthy);
        node.setLastUpdatedTimeStamp(System.currentTimeMillis());
        val payload = MAPPER.writeValueAsBytes(
                ServiceNodesResponse.<NodeData>builder()
                        .data(Collections.singletonList(node))
                        .build());
        server.stubFor(get(urlEqualTo("/ranger/nodes/v1/testns/test"))
                               .willReturn(aResponse()
                                                   .withBody(payload)
                                                   .withStatus(200)));
        val clientConfig = HttpClientConfig.builder()
                .host("127.0.0.1")
                .port(server.port())
                .connectionTimeoutMs(30_000)
                .operationTimeoutMs(30_000)
                .build();

        val finder = new HttpShardedServiceFinderBuilder<NodeData>()
                .withClientConfig(clientConfig)
                .withNamespace("testns")
                .withServiceName("test")
                .withObjectMapper(MAPPER)
                .withDeserializer(data -> {
                    try {
                        return MAPPER.readValue(data, new TypeReference<ServiceNodesResponse<NodeData>>() {});
                    }
                    catch (IOException e) {
                        throw new IllegalArgumentException(e);
                    }
                })
                .withShardSelector((criteria, registry) -> registry.nodeList())
                .withNodeRefreshIntervalMs(1000)
                .build();
        finder.start();
        RangerTestUtils.sleepUntilFinderStarts(finder);
        Assert.assertTrue(finder.get(nodeData -> true).isPresent());
    }

}