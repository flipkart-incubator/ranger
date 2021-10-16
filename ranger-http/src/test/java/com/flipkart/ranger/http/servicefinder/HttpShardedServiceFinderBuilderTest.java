package com.flipkart.ranger.http.servicefinder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.TestUtils;
import com.flipkart.ranger.core.finder.SimpleShardedServiceFinder;
import com.flipkart.ranger.core.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.units.TestNodeData;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.model.ServiceNodesResponse;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.Data;
import lombok.val;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

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
        final NodeData testNode = new NodeData("testNode");
        final ServiceNode<NodeData> node = new ServiceNode<>("127.0.0.1", 80, testNode);
        node.setHealthcheckStatus(HealthcheckStatus.healthy);
        node.setLastUpdatedTimeStamp(System.currentTimeMillis());
        val payload = MAPPER.writeValueAsBytes(
                ServiceNodesResponse.<NodeData>builder()
                        .node(node)
                        .success(true)
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

        final SimpleShardedServiceFinder<NodeData, Criteria<NodeData>> finder = new HttpShardedServiceFinderBuilder<NodeData, Criteria<NodeData>>()
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
                .build();
        finder.start();
        TestUtils.sleepForSeconds(3);
        Assert.assertNotNull(finder.get(nodeData -> true));
    }

}