package com.flipkart.ranger.finder;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.PathBuilder;
import com.flipkart.ranger.model.ServiceNode;
import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CuratorServiceRegistryUpdater<T> extends AbstractServiceRegistryUpdater<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractServiceRegistryUpdater.class);

    private CuratorSourceConfig config;
    private Deserializer<T> deserializer;
    private CuratorFramework curatorFramework;
    private String serviceName;

    protected CuratorServiceRegistryUpdater(CuratorSourceConfig config, Deserializer<T> deserializer, CuratorFramework curatorFramework, String serviceName){
        this.config = config;
        this.deserializer = deserializer;
        this.curatorFramework = curatorFramework;
        this.serviceName = serviceName;
    }

    @Override
    public void start() throws Exception {
        curatorFramework.start();

        //zookeeper cluster connection
        curatorFramework.blockUntilConnected();
        logger.debug("Connected to zookeeper cluster");
        curatorFramework.newNamespaceAwareEnsurePath(PathBuilder.path(serviceName))
                .ensure(curatorFramework.getZookeeperClient());

        curatorFramework.getChildren().usingWatcher(new CuratorWatcher() {
            @Override
            public void process(WatchedEvent event) throws Exception {
                switch (event.getType()) {

                    case NodeChildrenChanged: {
                        checkForUpdate();
                        break;
                    }
                    case None:
                    case NodeCreated:
                    case NodeDeleted:
                    case NodeDataChanged:
                        break;
                    default:
                        break;
                }
            }
        }).forPath(PathBuilder.path(serviceName)); //Start watcher on service node
        serviceRegistry.nodes(getHealthyServiceNodes());
        logger.info("Started polling zookeeper for changes");
    }

    public boolean isRunning() {
        return curatorFramework != null
                && (curatorFramework.getState() == CuratorFrameworkState.STARTED);
    }

    @Override
    protected List<ServiceNode<T>> getHealthyServiceNodes() {
        try {
            final long healthcheckZombieCheckThresholdTime = System.currentTimeMillis() - 60000; //1 Minute

            if(!isRunning()) {
                return null;
            }

            final String parentPath = PathBuilder.path(serviceName);
            List<String> children = curatorFramework.getChildren().forPath(parentPath);
            List<ServiceNode<T>> nodes = Lists.newArrayListWithCapacity(children.size());
            for(String child : children) {
                final String path = String.format("%s/%s", parentPath, child);
                if(null == curatorFramework.checkExists().forPath(path)) {
                    continue;
                }
                byte[] data = curatorFramework.getData().forPath(path);
                if(null == data) {
                    logger.warn("Not data present for node: " + path);
                    continue;
                }
                ServiceNode<T> key = deserializer.deserialize(data);
                if(HealthcheckStatus.healthy == key.getHealthcheckStatus()
                        && key.getLastUpdatedTimeStamp() > healthcheckZombieCheckThresholdTime) {
                    nodes.add(key);
                }
            }
            return nodes;
        } catch (Exception e) {
            logger.error("Error getting service data from zookeeper: ", e);
        }
        return null;
    }

    @Override
    public void stop() throws Exception {
        curatorFramework.close();
        logger.debug("Stopped updater");
    }

}
