package com.flipkart.ranger.healthservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.ServiceFinderBuilders;
import com.flipkart.ranger.ServiceProviderBuilders;
import com.flipkart.ranger.finder.unsharded.UnshardedClusterFinder;
import com.flipkart.ranger.finder.unsharded.UnshardedClusterInfo;
import com.flipkart.ranger.healthcheck.Healthcheck;
import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.healthservice.monitor.sample.RotationStatusMonitor;
import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.Serializer;
import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.serviceprovider.ServiceProvider;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServiceProviderIntegrationTest {

    final String filePath = "/tmp/rangerRotationFile.html";
    File file = new File(filePath);

    private TestingCluster testingCluster;
    private ObjectMapper objectMapper;

    ServiceHealthAggregator serviceHealthAggregator;
    UnshardedClusterFinder serviceFinder;

    @Before
    public void startTestCluster() throws Exception {
        serviceHealthAggregator = new ServiceHealthAggregator();
        serviceHealthAggregator.addMonitor(new RotationStatusMonitor(new TimeEntity(50, TimeUnit.MILLISECONDS), filePath));
        serviceHealthAggregator.start();
        objectMapper = new ObjectMapper();
        testingCluster = new TestingCluster(3);
        testingCluster.start();
        registerService("localhost-1", 9000, 1);
        registerService("localhost-2", 9000, 1);
        registerService("localhost-3", 9000, 2);

        serviceFinder = ServiceFinderBuilders.unshardedFinderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withDeserializer(new Deserializer<UnshardedClusterInfo>() {
                    @Override
                    public ServiceNode<UnshardedClusterInfo> deserialize(byte[] data) {
                        try {
                            return objectMapper.readValue(data,
                                    new TypeReference<ServiceNode<UnshardedClusterInfo>>() {
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .build();
        serviceFinder.start();
    }

    @After
    public void stopTestCluster() throws Exception {
        if(null != testingCluster) {
            testingCluster.close();
        }
        serviceFinder.stop();
    }

    @Test
    public void testBasicDiscovery() throws Exception {
        final boolean filecreate = file.createNewFile();
        Thread.sleep(2000);
        List<ServiceNode<UnshardedClusterInfo>> all = serviceFinder.getAll(null);
        System.out.println("all = " + all);
        Assert.assertEquals(3, all.size());
        final boolean delete = file.delete();
        Thread.sleep(2000);
        all = serviceFinder.getAll(null);
        System.out.println("all = " + all);
        Assert.assertEquals(0, all.size());
    }

    private void registerService(String host, int port, int shardId) throws Exception {
        ServiceProvider<UnshardedClusterInfo> serviceProvider = ServiceProviderBuilders.unshardedServiceProviderBuilder()
                .withConnectionString(testingCluster.getConnectString())
                .withNamespace("test")
                .withServiceName("test-service")
                .withSerializer(new Serializer<UnshardedClusterInfo>() {
                    @Override
                    public byte[] serialize(ServiceNode<UnshardedClusterInfo> data) {
                        try {
                            return objectMapper.writeValueAsBytes(data);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .withHostname(host)
                .withPort(port)
                .withHealthcheck(new Healthcheck() {
                    @Override
                    public HealthcheckStatus check() {
                        return serviceHealthAggregator.getServiceHealth();
                    }
                })
                .buildServiceDiscovery();
        serviceProvider.start();
    }
}
