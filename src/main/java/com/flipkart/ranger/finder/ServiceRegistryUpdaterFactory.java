package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.ServiceRegistry;

public class ServiceRegistryUpdaterFactory<T> {
    private ServiceRegistry<T> serviceRegistry;

    public ServiceRegistryUpdaterFactory(ServiceRegistry<T> serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public AbstractServiceRegistryUpdater<T> getServiceRegistryUpdater(Service service) {
        return service.accept(new ServiceVisitor<AbstractServiceRegistryUpdater<T>>() {
            @Override
            public AbstractServiceRegistryUpdater<T> visit(CuratorService curatorService) {
                return new CuratorServiceRegistryUpdater<T>(serviceRegistry, curatorService);
            }

            @Override
            public AbstractServiceRegistryUpdater<T> visit(HttpService httpService) {
                return new HttpServiceRegistryUpdater<T>(serviceRegistry, httpService);
            }
        });

    }
}
