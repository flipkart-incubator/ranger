package com.flipkart.ranger.datasource;

import com.flipkart.ranger.finder.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Slf4j
public class ScheduledRegistryUpdateSignalGenerator<T> extends RegistryUpdateSignalGenerator<T> {
    private final long refreshIntervalMillis;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture = null;

    public ScheduledRegistryUpdateSignalGenerator(
            Service service,
            NodeDataSource<T> dataSource,
            long refreshIntervalMillis) {
        super(service, dataSource);
        this.refreshIntervalMillis = refreshIntervalMillis;
    }

    @Override
    public void start() {
        scheduledFuture = scheduler.scheduleWithFixedDelay(() -> {
            try {
                onSignalReceived();
            } catch (Exception e) {
                log.error("Error checking for updates from zk for service:" + getService().getServiceName() , e);
            }
        }, 0, refreshIntervalMillis, TimeUnit.MILLISECONDS);
        log.info("Started scheduled registry updater for service {}", getService().getServiceName());
    }

    @Override
    public void shutdown() {
        if(null != scheduledFuture) {
            scheduledFuture.cancel(true);
        }
        log.info("Shut down scheduled registry updater for service {}", getService().getServiceName());
    }
}
