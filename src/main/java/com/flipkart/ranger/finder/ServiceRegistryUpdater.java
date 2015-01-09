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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceRegistryUpdater<T> implements Callable<Void> {
    private ServiceRegistry<T> serviceRegistry;

    private Lock checkLock = new ReentrantLock();
    private Condition checkCondition = checkLock.newCondition();
    private boolean checkForUpdate = false;

    public ServiceRegistryUpdater(ServiceRegistry<T> serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void start() throws Exception {
        CuratorFramework curatorFramework = serviceRegistry.getService().getCuratorFramework();
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
                }
            }
        }).forPath(PathBuilder.path(serviceRegistry.getService())); //Start watcher on service node
        serviceRegistry.nodes(checkForUpdateOnZookeeper());
    }

    @Override
    public Void call() throws Exception {
        //Start checking for updates
        while (true) {
            try {
                checkLock.lock();
                while (!checkForUpdate) {
                    checkCondition.await();
                }
                serviceRegistry.nodes(checkForUpdateOnZookeeper());
                checkForUpdate =false;
            } finally {
                checkLock.unlock();
            }
        }
    }

    public void checkForUpdate() {
        try {
            checkLock.lock();
            checkForUpdate = true;
            checkCondition.signalAll();
        } finally {
            checkLock.unlock();
        }
    }

    private List<ServiceNode<T>> checkForUpdateOnZookeeper() {
        try {
            final Service service = serviceRegistry.getService();
            final Deserializer<T> deserializer = serviceRegistry.getDeserializer();
            final CuratorFramework curatorFramework = service.getCuratorFramework();
            final String parentPath = PathBuilder.path(service);
            List<String> children = curatorFramework.getChildren().forPath(parentPath);
            List<ServiceNode<T>> nodes = Lists.newArrayListWithCapacity(children.size());
            for(String child : children) {
                byte data[] = curatorFramework.getData().forPath(String.format("%s/%s", parentPath, child));
                if(null == data) {
                    continue; //TODO::LOG
                }
                ServiceNode<T> key = deserializer.deserialize(data);
                if(HealthcheckStatus.healthy == key.getHealthcheckStatus()) {
                    nodes.add(key);
                }
            }
            return nodes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
