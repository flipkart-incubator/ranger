package com.flipkart.ranger.http.serviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.TestUtils;
import com.flipkart.ranger.core.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.core.healthcheck.Healthchecks;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.serviceprovider.ServiceProvider;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.model.ServiceNodesResponse;
import com.flipkart.ranger.http.model.ServiceRegistrationResponse;
import com.flipkart.ranger.http.serde.HttpRequestDataSerializer;
import com.flipkart.ranger.http.servicefinder.HttpShardedServiceFinderBuilderTest;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.Builder;
import lombok.Data;
import lombok.val;
import okhttp3.RequestBody;
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
        TestNodeData nm5NodeData = TestNodeData.builder().farmId("nm5").build();
        ServiceNode<TestNodeData> testNode = new ServiceNode<>("localhost-1", 80, nm5NodeData);
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
        ServiceProvider<TestNodeData, HttpRequestDataSerializer<TestNodeData>> serviceProvider = new HttpShardedServiceProviderBuilder<TestNodeData>()
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
