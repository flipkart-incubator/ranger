package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.ServiceRegistry;

public class ServiceRegistryUpdaterFactory<T> {
    private SourceConfig config;
    private Deserializer<T> deserializer;

    public ServiceRegistryUpdaterFactory(SourceConfig config, Deserializer<T> deserializer) {
        this.config = config;
        this.deserializer = deserializer;
    }

    public AbstractServiceRegistryUpdater<T> getServiceRegistryUpdater(SourceConfig config) {
        return config.accept(new ServiceVisitor<AbstractServiceRegistryUpdater<T>>() {
            @Override
            public AbstractServiceRegistryUpdater<T> visit(CuratorSourceConfig curatorSourceConfig) {
                return new CuratorServiceRegistryUpdater<T>(curatorSourceConfig, deserializer);
            }

            @Override
            public AbstractServiceRegistryUpdater<T> visit(HttpSourceConfig httpSourceConfig) {
                return new HttpServiceRegistryUpdater<T>(httpSourceConfig, deserializer);
            }
        });

    }
}
