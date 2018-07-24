package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public abstract class AbstractServiceRegistry<T> extends ServiceRegistry<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractServiceRegistry.class);
    private int refreshIntervalMillis;
    private AbstractServiceRegistryUpdater<T> updater;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;
    private Service service;

    protected AbstractServiceRegistry(Service service, Deserializer<T> deserializer, int refreshIntervalMillis) {
        super(deserializer);
        this.service = service;
        this.refreshIntervalMillis = refreshIntervalMillis;
    }

    @Override
    public void start() throws Exception {
        service.start();
        ServiceRegistryUpdaterFactory<T> serviceRegistryUpdaterFactory= new ServiceRegistryUpdaterFactory<T>(this);
        updater = serviceRegistryUpdaterFactory.getServiceRegistryUpdater(service);
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
        service.stop();
        logger.debug("Service Registry stopped");
    }
}
