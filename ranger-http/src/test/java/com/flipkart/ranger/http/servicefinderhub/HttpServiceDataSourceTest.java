/**
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
package com.flipkart.ranger.http.servicefinderhub;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.model.ServiceDataSourceResponse;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import lombok.val;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class HttpServiceDataSourceTest {

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
    public void testServiceDataSource() throws IOException {
        val responseObj = ServiceDataSourceResponse.builder()
                .success(true)
                .data(Lists.newArrayList(
                        new Service("test-n", "test-s"),
                        new Service("test-n", "test-s1"),
                        new Service("test-n", "test-s2")
                ))
                .build();
        val response = MAPPER.writeValueAsBytes(responseObj);
        server.stubFor(get(urlEqualTo("/v1/ranger/services"))
                .willReturn(aResponse()
                        .withBody(response)
                        .withStatus(200)));
        val clientConfig = HttpClientConfig.builder()
                .host("127.0.0.1")
                .port(server.port())
                .connectionTimeoutMs(30_000)
                .operationTimeoutMs(30_000)
                .build();
        HttpServiceDataSource<TestNodeData> httpServiceDataSource = new HttpServiceDataSource<>(clientConfig, MAPPER);
        Collection<Service> services = httpServiceDataSource.services();
        Assert.assertNotNull(services);
        Assert.assertFalse(services.isEmpty());
        Assert.assertEquals(3, services.size());
        Assert.assertFalse(services.stream().noneMatch(each -> each.getServiceName().equalsIgnoreCase("test-s")));
        Assert.assertFalse(services.stream().noneMatch(each -> each.getServiceName().equalsIgnoreCase("test-s1")));
        Assert.assertFalse(services.stream().noneMatch(each -> each.getServiceName().equalsIgnoreCase("test-s2")));
    }

}
