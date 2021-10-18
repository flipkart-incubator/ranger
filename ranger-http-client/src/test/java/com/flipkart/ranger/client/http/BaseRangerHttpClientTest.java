package com.flipkart.ranger.client.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.core.util.Exceptions;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.model.ServiceDataSourceResponse;
import com.flipkart.ranger.http.model.ServiceNodesResponse;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.Lists;
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
        final TestNodeData testNode = new TestNodeData(1);
        final ServiceNode<TestNodeData> node = new ServiceNode<>("127.0.0.1", 80, testNode);
        node.setHealthcheckStatus(HealthcheckStatus.healthy);
        node.setLastUpdatedTimeStamp(System.currentTimeMillis());
        val payload = objectMapper.writeValueAsBytes(
                ServiceNodesResponse.<TestNodeData>builder()
                        .data(Lists.newArrayList(node))
                        .success(true)
                        .build());
        server.stubFor(get(urlEqualTo("/v1/ranger/nodes/test-n/test-s"))
                .willReturn(aResponse()
                        .withBody(payload)
                        .withStatus(200)));

        val responseObj = ServiceDataSourceResponse.builder()
                .success(true)
                .data(Lists.newArrayList(
                        new Service("test-n", "test-s")
                ))
                .build();
        val response = objectMapper.writeValueAsBytes(responseObj);
        server.stubFor(get(urlEqualTo("/v1/ranger/services"))
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
