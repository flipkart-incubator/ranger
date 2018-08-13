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

package com.flipkart.ranger.httpservicefinder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.ServiceFinderBuilders;
import com.flipkart.ranger.finder.HttpVerb.GetHttpVerb;
import com.flipkart.ranger.finder.HttpSourceConfig;
import com.flipkart.ranger.finder.sharded.SimpleShardedServiceFinder;
import com.flipkart.ranger.model.HttpResponseDecoder;
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

public class ShardedBasicTest {
    private static final Logger logger = LoggerFactory.getLogger(ShardedBasicTest.class);
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
        String json = "[{\"host\":\"localhost\",\"port\":31649,\"nodeData\":{\"shardId\":1},\"healthcheckStatus\":\"healthy\",\"lastUpdatedTimeStamp\":"+ Long.toString(timeStampMillis) + "}, {\"host\":\"localhost\",\"port\":31648,\"nodeData\":{\"shardId\":1},\"healthcheckStatus\":\"healthy\",\"lastUpdatedTimeStamp\":"+ Long.toString(timeStampMillis) + "}, {\"host\":\"localhost\",\"port\":31647,\"nodeData\":{\"shardId\":1},\"healthcheckStatus\":\"unhealthy\",\"lastUpdatedTimeStamp\":"+ Long.toString(timeStampMillis) + "}]";
        stubFor(get(urlEqualTo("/testsharded")).willReturn(aResponse().withBody(json)));
    }

    @After
    public void stopTestCluster() throws Exception {
        wireMockServer.stop();
    }

    private static final class TestShardInfo {
        private int shardId;

        public TestShardInfo(int shardId) {
            this.shardId = shardId;
        }

        public TestShardInfo() {
        }

        public int getshardId() {
            return shardId;
        }

        public void setEnvironment(String environment) {
            this.shardId = shardId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ShardedBasicTest.TestShardInfo that = (ShardedBasicTest.TestShardInfo) o;

            if (shardId != that.shardId) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return shardId;
        }
    }

    @Test
    public void testBasicDiscovery() throws Exception {
        HttpResponseDecoder<TestShardInfo> deserializer = new HttpResponseDecoder<TestShardInfo>() {
            @Override
            public List<ServiceNode<TestShardInfo>> deserialize(byte[] data) {
                try {
                    return objectMapper.readValue(data,
                            new TypeReference<List<ServiceNode<TestShardInfo>>>() {
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        HttpSourceConfig<TestShardInfo> httpSourceConfig = new HttpSourceConfig<TestShardInfo>("localhost", 8080, "/testsharded", deserializer, false, false, new GetHttpVerb());

        SimpleShardedServiceFinder serviceFinder = ServiceFinderBuilders.<TestShardInfo>shardedFinderBuilder()
                .withSourceConfig(httpSourceConfig)
                .build();

        serviceFinder.start();

        List<ServiceNode<TestShardInfo>> nodesList = serviceFinder.getAll(new TestShardInfo(1));
        Assert.assertEquals(2, nodesList.size());
    }
}
