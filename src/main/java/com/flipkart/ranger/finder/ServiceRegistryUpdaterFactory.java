package com.flipkart.ranger.finder;

import com.google.common.base.Preconditions;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ServiceRegistryUpdaterFactory<T> {

    public ServiceRegistryUpdater<T> getServiceRegistryUpdater(SourceConfig<T> sourceConfig) throws Exception{
        return sourceConfig.accept(new SourceConfigVisitor<T>() {
            @Override
            public ServiceRegistryUpdater<T> visit(CuratorFrameworkConfig<T> curatorFrameworkConfig) throws Exception{
                Preconditions.checkNotNull(curatorFrameworkConfig.getCuratorFramework());
                Preconditions.checkNotNull(curatorFrameworkConfig.getDeserializer());
                Preconditions.checkNotNull(curatorFrameworkConfig.getServiceName());
                return new CuratorServiceRegistryUpdater<T>(curatorFrameworkConfig.getDeserializer(),
                        curatorFrameworkConfig.getCuratorFramework(), curatorFrameworkConfig.getServiceName());
            }

            @Override
            public ServiceRegistryUpdater<T> visit(HttpSourceConfig<T> httpSourceConfig) throws Exception{
                Preconditions.checkNotNull(httpSourceConfig.getHost());
                Preconditions.checkNotNull(httpSourceConfig.getPort());
                Preconditions.checkNotNull(httpSourceConfig.getHttpResponseDecoder());
                Preconditions.checkArgument((httpSourceConfig.getPort() > 0 && httpSourceConfig.getPort() < 65535));
                return new HttpServiceRegistryUpdater<T>(httpSourceConfig);
            }

            @Override
            public ServiceRegistryUpdater<T> visit(ZookeeperSourceConfig<T> zookeeperSourceConfig) throws Exception{
                Preconditions.checkNotNull(zookeeperSourceConfig.getNamespace());
                Preconditions.checkNotNull(zookeeperSourceConfig.getConnectionString());
                Preconditions.checkNotNull(zookeeperSourceConfig.getZookeeperNodeDataDecoder());
                Preconditions.checkNotNull(zookeeperSourceConfig.getServiceName());
                CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                        .namespace(zookeeperSourceConfig.getNamespace())
                        .connectString(zookeeperSourceConfig.getConnectionString())
                        .retryPolicy(new ExponentialBackoffRetry(1000, 100)).build();
                curatorFramework.start();
                return new CuratorServiceRegistryUpdater<T>(zookeeperSourceConfig.getZookeeperNodeDataDecoder(),
                        curatorFramework, zookeeperSourceConfig.getServiceName());
            }
        });

    }
}
