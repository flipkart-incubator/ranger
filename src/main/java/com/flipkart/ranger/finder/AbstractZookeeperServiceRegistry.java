package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.PathBuilder;
import com.flipkart.ranger.model.ServiceRegistry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractZookeeperServiceRegistry<T> extends ServiceRegistry<T> {
    private ServiceRegistryUpdater<T> updater;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    protected AbstractZookeeperServiceRegistry(Service service, Deserializer<T> deserializer) {
        super(service, deserializer);
    }

    @Override
    public void start() throws Exception {
        final Service service = getService();
        service.getCuratorFramework().start();
        service.getCuratorFramework().blockUntilConnected();
        System.out.println("Connected");
        service.getCuratorFramework().newNamespaceAwareEnsurePath(PathBuilder.path(service)).ensure(service.getCuratorFramework().getZookeeperClient());
        updater = new ServiceRegistryUpdater<T>(this);
        updater.start();
        executorService.submit(updater);
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    updater.checkForUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws Exception {
        getService().getCuratorFramework().close();
        //TODO
    }

}
