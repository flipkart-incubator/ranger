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
package com.flipkart.ranger.http.serviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.healthcheck.Healthchecks;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.model.ServiceRegistrationResponse;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.Builder;
import lombok.Data;
import lombok.val;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class HttpShardedServiceProviderBuilderTest {

    @Data
    private static final class TestNodeData {
        private final String farmId;

        @Builder
        public TestNodeData(@JsonProperty("farmId") String farmId) {
            this.farmId = farmId;
        }
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Rule
    public WireMockRule server = new WireMockRule(8888);

    @Test
    public void testProvider() throws Exception {
        val nm5NodeData = TestNodeData.builder().farmId("nm5").build();
        val testNode = new ServiceNode<>("localhost-1", 80, nm5NodeData);
        val response = MAPPER.writeValueAsBytes(
                ServiceRegistrationResponse.builder()
                        .success(true)
                        .build());
        byte[] requestBytes = MAPPER.writeValueAsBytes(testNode);
        server.stubFor(post(urlEqualTo("/ranger/nodes/v1/add/testns/test"))
                .withRequestBody(binaryEqualTo(requestBytes))
                .willReturn(aResponse()
                        .withBody(response)
                        .withStatus(200)));
        val clientConfig = HttpClientConfig.builder()
                .host("127.0.0.1")
                .port(server.port())
                .connectionTimeoutMs(30_000)
                .operationTimeoutMs(30_000)
                .build();
        val serviceProvider = new HttpShardedServiceProviderBuilder<TestNodeData>()
                .withNamespace("testns")
                .withServiceName("test")
                .withHostname("localhost-1")
                .withPort(80)
                .withHealthcheck(Healthchecks.defaultHealthyCheck())
                .withHealthUpdateIntervalMs(1000)
                .withObjectMapper(MAPPER)
                .withClientConfiguration(clientConfig)
                .withNodeData(nm5NodeData)
                .withSerializer(node -> requestBytes)
                .build();
        serviceProvider.start();
    }

}
