package com.flipkart.ranger.zookeeper.servicefinderhub;

import com.flipkart.ranger.core.finderhub.ServiceFinderHub;
import com.flipkart.ranger.core.model.ServiceRegistry;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 *
 */
@Slf4j
public class ZkServiceFinderHubBuilder<T, R extends ServiceRegistry<T>> extends ServiceFinderHubBuilder<T, R> {
    private String namespace;
    private CuratorFramework curatorFramework;
    private String connectionString;

    public ZkServiceFinderHubBuilder<T, R> withNamespace(final String namespace) {
        this.namespace = namespace;
        return this;
    }

    public ZkServiceFinderHubBuilder<T, R> withCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
        return this;
    }

    public ZkServiceFinderHubBuilder<T, R> withConnectionString(final String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    @Override
    protected void preBuild() {
        if (null == curatorFramework) {
            Preconditions.checkNotNull(connectionString);
            log.info("Building custom curator framework");
            curatorFramework = CuratorFrameworkFactory.builder()
                    .namespace(namespace)
                    .connectString(connectionString)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 100))
                    .build();
            super.withExtraStartSignalConsumer(x -> curatorFramework.start());
            super.withExtraStartSignalConsumer(x -> curatorFramework.close());
        }
    }

    @Override
    protected void postBuild(ServiceFinderHub<T, R> serviceFinderHub) {
        log.debug("No post build steps necessary");
    }
}
