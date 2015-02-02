package com.flipkart.ranger.serviceprovider;

import com.flipkart.ranger.healthcheck.HealthChecker;
import com.flipkart.ranger.healthcheck.Healthcheck;
import com.flipkart.ranger.model.Serializer;
import com.flipkart.ranger.model.ServiceNode;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

//TODO::INTERFACE
public class ServiceProvider<T> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProvider.class);

    private String serviceName;
    private Serializer<T> serializer;
    private CuratorFramework curatorFramework;
    private ServiceNode<T> serviceNode;
    private List<Healthcheck> healthchecks;
    private final int refreshInterval;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> future;

    public ServiceProvider(String serviceName, Serializer<T> serializer,
                           CuratorFramework curatorFramework,
                           ServiceNode<T> serviceNode,
                           List<Healthcheck> healthchecks,
                           int refreshInterval) {
        this.serviceName = serviceName;
        this.serializer = serializer;
        this.curatorFramework = curatorFramework;
        this.serviceNode = serviceNode;
        this.healthchecks = healthchecks;
        this.refreshInterval = refreshInterval;
    }

    public void updateState(ServiceNode<T> serviceNode) throws Exception {
        curatorFramework.setData().forPath(
                String.format("/%s/%s", serviceName, serviceNode.representation()),
                serializer.serialize(serviceNode));
    }

    public void start() throws Exception {
        curatorFramework.blockUntilConnected();
        curatorFramework.newNamespaceAwareEnsurePath(String.format("/%s", serviceName)).ensure(curatorFramework.getZookeeperClient());
        logger.debug("Connected to zookeeper");
        curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(
                String.format("/%s/%s", serviceName, serviceNode.representation()),
                serializer.serialize(serviceNode));
        logger.debug("Set initial node data on zookeeper.");
        future = executorService.scheduleWithFixedDelay(new HealthChecker<T>(healthchecks, this), 0, refreshInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() throws Exception {
        if(null != future) {
            future.cancel(true);
        }
        curatorFramework.close();
    }

    public ServiceNode<T> getServiceNode() {
        return serviceNode;
    }
}
