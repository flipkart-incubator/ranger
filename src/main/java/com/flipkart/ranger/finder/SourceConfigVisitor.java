package com.flipkart.ranger.finder;

public interface SourceConfigVisitor<T> {
    ServiceRegistryUpdater<T> visit(HttpSourceConfig<T> httpSourceConfig) throws Exception;
    ServiceRegistryUpdater<T> visit(ZookeeperSourceConfig<T> zookeeperSourceConfig) throws Exception;
    ServiceRegistryUpdater<T> visit(CuratorFrameworkConfig<T> curatorFrameworkConfig) throws Exception;
}