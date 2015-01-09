package com.flipkart.ranger.serviceprovider;

import com.flipkart.ranger.healthcheck.Healthcheck;
import com.flipkart.ranger.model.Serializer;
import com.flipkart.ranger.model.ServiceNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.List;

public class ServiceProviderBuilder<T> {
    private String namespace;
    private String serviceName;
    private CuratorFramework curatorFramework;
    private String connectionString;
    private Serializer<T> serializer;
    private String hostname;
    private int port;
    private T nodeData;
    private List<Healthcheck> healthchecks = Lists.newArrayList();

    public ServiceProviderBuilder<T> withNamespace(final String namespace) {
        this.namespace = namespace;
        return this;
    }

    public ServiceProviderBuilder<T> withServiceName(final String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ServiceProviderBuilder<T> withCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
        return this;
    }

    public ServiceProviderBuilder<T> withConnectionString(final String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    public ServiceProviderBuilder<T> withSerializer(Serializer<T> deserializer) {
        this.serializer = deserializer;
        return this;
    }

    public ServiceProviderBuilder<T> withHostname(final String hostname) {
        this.hostname = hostname;
        return this;
    }

    public ServiceProviderBuilder<T> withPort(int port) {
        this.port = port;
        return this;
    }

    public ServiceProviderBuilder<T> withNodeData(T nodeData) {
        this.nodeData = nodeData;
        return this;
    }

    public ServiceProviderBuilder<T> withHealthcheck(Healthcheck healthcheck) {
        this.healthchecks.add(healthcheck);
        return this;
    }

    public ServiceProvider<T> buildServiceDiscovery() {
        Preconditions.checkNotNull(namespace);
        Preconditions.checkNotNull(serviceName);
        Preconditions.checkNotNull(serializer);
        Preconditions.checkNotNull(hostname);
        Preconditions.checkArgument(port > 0);
        Preconditions.checkArgument(!healthchecks.isEmpty());
        if( null == curatorFramework) {
            Preconditions.checkNotNull(connectionString);
            curatorFramework = CuratorFrameworkFactory.builder()
                    .namespace(namespace)
                    .connectString(connectionString)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 100)).build();
        }
        return new ServiceProvider<T>(serviceName, serializer, curatorFramework, new ServiceNode<T>(hostname, port, nodeData), healthchecks);
    }

}
