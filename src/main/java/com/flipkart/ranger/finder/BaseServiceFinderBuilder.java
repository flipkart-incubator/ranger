/**
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.ranger.finder;

import com.flipkart.ranger.datasource.ZookeeperNodeDataSource;
import com.flipkart.ranger.datasource.signals.ScheduledRegistryUpdateSignalGenerator;
import com.flipkart.ranger.datasource.signals.ZookeeperWatcherRegistryUpdateSignalGenerator;
import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.ServiceNodeSelector;
import com.flipkart.ranger.model.ServiceRegistry;
import com.flipkart.ranger.model.ShardSelector;
import com.flipkart.ranger.signals.SignalGenerator;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public abstract class BaseServiceFinderBuilder<T, RegistryType extends ServiceRegistry<T>, FinderType extends ServiceFinder<T, RegistryType>> {

    private String namespace;
    private String serviceName;
    private CuratorFramework curatorFramework;
    private String connectionString;
    private int nodeRefreshIntervalMs;
    private boolean disablePushUpdaters;
    private Deserializer<T> deserializer;
    private ShardSelector<T, RegistryType> shardSelector;
    private ServiceNodeSelector<T> nodeSelector = new RandomServiceNodeSelector<>();
    private final List<SignalGenerator<T>> additionalRefreshSignals = new ArrayList<>();

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withNamespace(final String namespace) {
        this.namespace = namespace;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withServiceName(final String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withConnectionString(final String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withDeserializer(Deserializer<T> deserializer) {
        this.deserializer = deserializer;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withShardSelector(ShardSelector<T, RegistryType> shardSelector) {
        this.shardSelector = shardSelector;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withNodeSelector(ServiceNodeSelector<T> nodeSelector) {
        this.nodeSelector = nodeSelector;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withNodeRefreshIntervalMs(int nodeRefreshIntervalMs) {
        this.nodeRefreshIntervalMs = nodeRefreshIntervalMs;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withDisableWatchers() {
        this.disablePushUpdaters = true;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withDisableWatchers(boolean disablePushUpdaters) {
        this.disablePushUpdaters = disablePushUpdaters;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withAdditionalSignalGenerator(SignalGenerator<T> signalGenerator) {
        this.additionalRefreshSignals.add(signalGenerator);
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withAdditionalSignalGenerators(SignalGenerator<T> ...signalGenerators) {
        this.additionalRefreshSignals.addAll(Arrays.asList(signalGenerators));
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withAdditionalSignalGenerators(List<SignalGenerator<T>> signalGenerators) {
        this.additionalRefreshSignals.addAll(signalGenerators);
        return this;
    }

    public FinderType build() {
        Preconditions.checkNotNull(namespace);
        Preconditions.checkNotNull(serviceName);
        Preconditions.checkNotNull(deserializer);
        if (null == curatorFramework) {
            Preconditions.checkNotNull(connectionString);
            curatorFramework = CuratorFrameworkFactory.builder()
                    .namespace(namespace)
                    .connectString(connectionString)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 100)).build();
            curatorFramework.start();
        }
        if (nodeRefreshIntervalMs < 1000) {
            log.warn("Node refresh interval for {} is too low: {} ms. Has been upgraded to 1000ms ",
                     serviceName, nodeRefreshIntervalMs);
            nodeRefreshIntervalMs = 1000;
        }
        Service service = new Service(namespace, serviceName);
        val finder = buildFinder(service, shardSelector, nodeSelector);
        val registry = finder.getServiceRegistry();
        List<SignalGenerator<T>> signalGenerators = new ArrayList<>();
        final ZookeeperNodeDataSource<T> zookeeperNodeDataSource = new ZookeeperNodeDataSource<>(service,
                                                                                                 null,
                                                                                                 deserializer,
                                                                                                 curatorFramework);
        signalGenerators.add(new ScheduledRegistryUpdateSignalGenerator<>(service, nodeRefreshIntervalMs));
        if (!disablePushUpdaters) {
            signalGenerators.add(new ZookeeperWatcherRegistryUpdateSignalGenerator<>(service,
                                                                                     zookeeperNodeDataSource,
                                                                                     curatorFramework));
        }
        else {
            log.info("Push based signal updater not registered for service: {}", service.getServiceName());
        }

        if(!additionalRefreshSignals.isEmpty()) {
            signalGenerators.addAll(additionalRefreshSignals);
            log.debug("Added additional signal handlers");
        }

        val updater = new ServiceRegistryUpdater<T>(registry, zookeeperNodeDataSource, signalGenerators);
        finder.registerUpdater(updater);
        return finder;
    }

    protected abstract FinderType buildFinder(
            Service service,
            ShardSelector<T, RegistryType> shardSelector,
            ServiceNodeSelector<T> nodeSelector);

}
