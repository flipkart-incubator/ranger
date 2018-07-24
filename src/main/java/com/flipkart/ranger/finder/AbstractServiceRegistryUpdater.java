package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.model.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractServiceRegistryUpdater<T> implements Callable<Void> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);
    private ServiceRegistry<T> serviceRegistry;

    public abstract void start() throws Exception;
    public abstract void stop();

    private Lock checkLock = new ReentrantLock();
    private Condition checkCondition = checkLock.newCondition();
    private boolean checkForUpdate = false;

    public AbstractServiceRegistryUpdater(ServiceRegistry<T> serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
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
                List<ServiceNode<T>> nodes = getServiceNodes();
                if(null != nodes) {
                    logger.debug("Setting nodelist of size: " + nodes.size());
                    serviceRegistry.nodes(nodes);
                }
                else {
                    logger.warn("No service shards/nodes found. We are disconnected from zookeeper. Keeping old list.");
                }
                checkForUpdate =false;
            } finally {
                checkLock.unlock();
            }
        }
    }

    protected abstract List<ServiceNode<T>> getServiceNodes();

    public void checkForUpdate() {
        try {
            checkLock.lock();
            checkForUpdate = true;
            checkCondition.signalAll();
        } finally {
            checkLock.unlock();
        }
    }
}
