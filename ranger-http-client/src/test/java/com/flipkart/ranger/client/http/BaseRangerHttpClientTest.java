/*
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.ranger.client.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.util.Exceptions;
import com.flipkart.ranger.core.utils.RangerTestUtils;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.model.ServiceDataSourceResponse;
import com.flipkart.ranger.http.model.ServiceNodesResponse;
import com.flipkart.ranger.http.response.model.RangerResponseCode;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Slf4j
@Getter
public abstract class BaseRangerHttpClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Rule
    public WireMockRule server = new WireMockRule(8888);
    private HttpClientConfig httpClientConfig;

    @Before
    public void startTestCluster() throws Exception {
        val testNode = TestNodeData.builder().shardId(1).build();
        val node = new ServiceNode<>("127.0.0.1", 80, testNode);
        node.setHealthcheckStatus(HealthcheckStatus.healthy);
        node.setLastUpdatedTimeStamp(System.currentTimeMillis());
        val payload = objectMapper.writeValueAsBytes(
                ServiceNodesResponse.<TestNodeData>builder()
                        .data(Lists.newArrayList(node))
                        .code(RangerResponseCode.SUCCESS)
                        .build());
        server.stubFor(get(urlEqualTo("/ranger/nodes/v1/test-n/test-s"))
                .willReturn(aResponse()
                        .withBody(payload)
                        .withStatus(200)));

        val responseObj = ServiceDataSourceResponse.builder()
                .code(RangerResponseCode.SUCCESS)
                .data(Sets.newHashSet(
                        RangerTestUtils.getService("test-n", "test-s")
                ))
                .build();
        val response = objectMapper.writeValueAsBytes(responseObj);
        server.stubFor(get(urlEqualTo("/ranger/services/v1"))
                .willReturn(aResponse()
                        .withBody(response)
                        .withStatus(200)));

        httpClientConfig = HttpClientConfig.builder()
                .host("127.0.0.1")
                .port(server.port())
                .connectionTimeoutMs(30_000)
                .operationTimeoutMs(30_000)
                .build();
        log.debug("Started http subsystem");
    }

    @After
    public void stopTestCluster() {
        log.debug("Stopping http subsystem");
    }

    protected ServiceNodesResponse<TestNodeData> read(final byte[] data) {
        try {
            return getObjectMapper().readValue(data, new TypeReference<ServiceNodesResponse<TestNodeData>>() {});
        }
        catch (IOException e) {
            Exceptions.illegalState(e);
        }
        return null;
    }
}
