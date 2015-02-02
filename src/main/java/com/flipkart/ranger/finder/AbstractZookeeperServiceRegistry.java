package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.PathBuilder;
import com.flipkart.ranger.model.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public abstract class AbstractZookeeperServiceRegistry<T> extends ServiceRegistry<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractZookeeperServiceRegistry.class);
    private int refreshIntervalMillis;
    private ServiceRegistryUpdater<T> updater;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;

    protected AbstractZookeeperServiceRegistry(Service service, Deserializer<T> deserializer, int refreshIntervalMillis) {
        super(service, deserializer);
        this.refreshIntervalMillis = refreshIntervalMillis;
    }

    @Override
    public void start() throws Exception {
        final Service service = getService();
        service.getCuratorFramework().blockUntilConnected();
        logger.debug("Connected to zookeeper cluster");
        service.getCuratorFramework().newNamespaceAwareEnsurePath(PathBuilder.path(service))
                                    .ensure(service.getCuratorFramework().getZookeeperClient());
        updater = new ServiceRegistryUpdater<T>(this);
        updater.start();
        executorService.submit(updater);
        scheduledFuture = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    updater.checkForUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, refreshIntervalMillis, TimeUnit.MILLISECONDS);
        logger.debug("Service Registry Started");
    }

    @Override
    public void stop() throws Exception {
        try {
            if( null != scheduledFuture ) {
                scheduledFuture.cancel(true);
            }
            updater.stop();
        } catch (Exception e) {
            logger.error("Error stopping ZK poller: ", e);
        }
        getService().getCuratorFramework().close();
        //TODO
        logger.debug("Service Registry Started");
    }

}
