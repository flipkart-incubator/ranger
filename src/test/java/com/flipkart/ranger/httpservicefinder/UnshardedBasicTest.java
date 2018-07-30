package com.flipkart.ranger.httpservicefinder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.ServiceFinderBuilders;
import com.flipkart.ranger.finder.HttpSourceConfig;
import com.flipkart.ranger.finder.unsharded.UnshardedClusterFinder;
import com.flipkart.ranger.finder.unsharded.UnshardedClusterInfo;
import com.flipkart.ranger.model.ListDeserializer;
import com.flipkart.ranger.model.ServiceNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class UnshardedBasicTest {
    private static final Logger logger = LoggerFactory.getLogger(UnshardedBasicTest.class);
    private ObjectMapper objectMapper;
    private WireMockServer wireMockServer;

    @Before
    public void startTestCluster() throws Exception{
        objectMapper = new ObjectMapper();
        wireMockServer = new WireMockServer();
        wireMockServer.start();
        configureFor("localhost", 8080);
        Instant instant = Instant.now();
        long timeStampMillis = instant.toEpochMilli();
        String json = "[{\"host\":\"localhost\",\"port\":31649,\"nodeData\":{\"environment\":\"stage\"},\"healthcheckStatus\":\"healthy\",\"lastUpdatedTimeStamp\":"+ Long.toString(timeStampMillis) + "}, {\"host\":\"localhost\",\"port\":31648,\"nodeData\":{\"environment\":\"stage\"},\"healthcheckStatus\":\"healthy\",\"lastUpdatedTimeStamp\":"+ Long.toString(timeStampMillis) + "}, {\"host\":\"localhost\",\"port\":31647,\"nodeData\":{\"environment\":\"stage\"},\"healthcheckStatus\":\"unhealthy\",\"lastUpdatedTimeStamp\":"+ Long.toString(timeStampMillis) + "}]";
        logger.debug(json);
        stubFor(get(urlEqualTo("/test")).willReturn(aResponse().withBody(json)));
    }

    @After
    public void stopTestCluster() throws Exception {
        wireMockServer.stop();
    }

    static class EnvNodeData extends UnshardedClusterInfo{
        private String environment;

        public EnvNodeData() {
        }

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }
    }

    @Test
    public void testBasicDiscovery() throws Exception {
        ListDeserializer<UnshardedClusterInfo> deserializer = new ListDeserializer<UnshardedClusterInfo>() {
            @Override
            public List<ServiceNode<UnshardedClusterInfo>> deserialize(byte[] data) {
                try {
                    return objectMapper.readValue(data,
                            new TypeReference<List<ServiceNode<EnvNodeData>>>() {
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        HttpSourceConfig<UnshardedClusterInfo> httpSourceConfig = new HttpSourceConfig<UnshardedClusterInfo>("localhost", 8080, "/test", deserializer);

        UnshardedClusterFinder serviceFinder = ServiceFinderBuilders.unshardedFinderBuilder()
                .withHttpSourceConfig(httpSourceConfig)
                .build();

        serviceFinder.start();

        List<ServiceNode<UnshardedClusterInfo>> nodesList = serviceFinder.getAll(null);

        Assert.assertEquals(2, nodesList.size());
    }
}
