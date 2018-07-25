package com.flipkart.ranger.finder;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.PathBuilder;
import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.model.ServiceRegistry;
import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CuratorServiceRegistryUpdater<T> extends AbstractServiceRegistryUpdater<T> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    private ServiceRegistry<T> serviceRegistry;
    private CuratorService service;

    protected CuratorServiceRegistryUpdater(ServiceRegistry<T> serviceRegistry, CuratorService service){
        super(serviceRegistry);
        this.serviceRegistry = serviceRegistry;
        this.service = service;
    }

    @Override
    public void start() throws Exception {
        CuratorFramework curatorFramework = service.getCuratorFramework();
        //CuratorFramework curatorFramework = serviceRegistry.getService().getCuratorFramework();
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
        }).forPath(PathBuilder.path(service)); //Start watcher on service node
        serviceRegistry.nodes(getServiceNodes());
        logger.info("Started polling zookeeper for changes");
    }

    @Override
    protected List<ServiceNode<T>> getServiceNodes() {
        try {
            final long healthcheckZombieCheckThresholdTime = System.currentTimeMillis() - 60000; //1 Minute
            //final Service service = serviceRegistry.getService();
            if(!service.isRunning()) {
                return null;
            }

            final Deserializer<T> deserializer = serviceRegistry.getDeserializer();
            final CuratorFramework curatorFramework = service.getCuratorFramework();
            final String parentPath = PathBuilder.path(service);
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
        logger.debug("Stopped updater");
    }

}
