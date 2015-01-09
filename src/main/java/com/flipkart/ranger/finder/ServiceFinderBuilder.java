package com.flipkart.ranger.finder;

import com.flipkart.ranger.finder.sharded.MapBasedServiceRegistry;
import com.flipkart.ranger.finder.sharded.MatchingShardSelector;
import com.flipkart.ranger.finder.sharded.SimpleShardedServiceFinder;
import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.ServiceNodeSelector;
import com.flipkart.ranger.model.ShardSelector;
import com.google.common.base.Preconditions;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ServiceFinderBuilder<T> {
    private String namespace;
    private String serviceName;
    private CuratorFramework curatorFramework;
    private String connectionString;
    private Deserializer<T> deserializer;
    private ShardSelector<T, MapBasedServiceRegistry<T>> shardSelector = new MatchingShardSelector<T>();
    private ServiceNodeSelector<T> nodeSelector = new RandomServiceNodeSelector<T>();

    public ServiceFinderBuilder<T> withNamespace(final String namespace) {
        this.namespace = namespace;
        return this;
    }

    public ServiceFinderBuilder<T> withServiceName(final String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ServiceFinderBuilder<T> withCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
        return this;
    }

    public ServiceFinderBuilder<T> withConnectionString(final String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    public ServiceFinderBuilder<T> withDeserializer(Deserializer<T> deserializer) {
        this.deserializer = deserializer;
        return this;
    }

    public ServiceFinderBuilder<T> withShardSelector(ShardSelector<T, MapBasedServiceRegistry<T>> shardSelector) {
        this.shardSelector = shardSelector;
        return this;
    }

    public ServiceFinderBuilder<T> withNodeSelector(ServiceNodeSelector<T> nodeSelector) {
        this.nodeSelector = nodeSelector;
        return this;
    }

    public SimpleShardedServiceFinder<T> buildSimpleShardedServiceFinder() throws Exception {
        Preconditions.checkNotNull(namespace);
        Preconditions.checkNotNull(serviceName);
        Preconditions.checkNotNull(deserializer);
        if( null == curatorFramework) {
            Preconditions.checkNotNull(connectionString);
            curatorFramework = CuratorFrameworkFactory.builder()
                    .namespace(namespace)
                    .connectString(connectionString)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 100)).build();
        }
        Service service = new Service(curatorFramework, namespace, serviceName);
        MapBasedServiceRegistry<T> serviceRegistry = new MapBasedServiceRegistry<T>(service, deserializer);

        return new SimpleShardedServiceFinder<T>(serviceRegistry, shardSelector, nodeSelector);
    }

}
