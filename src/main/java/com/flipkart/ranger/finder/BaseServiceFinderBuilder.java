/**
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.*;
import com.google.common.base.Preconditions;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public abstract class BaseServiceFinderBuilder<T, RegistryType extends ServiceRegistry<T>, FinderType extends ServiceFinder<T, RegistryType>> {
    private CuratorSourceConfig<T> curatorSourceConfig;
    private HttpSourceConfig<T> httpSourceConfig;
    private CuratorFrameworkConfig<T> curatorFrameworkConfig;

    private int healthcheckRefreshTimeMillis;
    private ShardSelector<T, RegistryType> shardSelector;
    private ServiceNodeSelector<T> nodeSelector = new RandomServiceNodeSelector<T>();

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withHttpSourceConfig(HttpSourceConfig<T> httpSourceConfig) {
        this.httpSourceConfig = httpSourceConfig;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withCuratorSourceConfig(CuratorSourceConfig<T> curatorSourceConfig) {
        this.curatorSourceConfig = curatorSourceConfig;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withCuratorFrameworkConfig(CuratorFrameworkConfig<T> curatorFrameworkConfig) {
        this.curatorFrameworkConfig = curatorFrameworkConfig;
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

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> witHhealthcheckRefreshTimeMillis(int healthcheckRefreshTimeMillis) {
        this.healthcheckRefreshTimeMillis = healthcheckRefreshTimeMillis;
        return this;
    }

    public FinderType build() throws Exception {
        if (0 == healthcheckRefreshTimeMillis) {
            healthcheckRefreshTimeMillis = 1000;
        }
        if (null != httpSourceConfig) {
            Preconditions.checkNotNull(httpSourceConfig.getHost());
            Preconditions.checkNotNull(httpSourceConfig.getPort());
            Preconditions.checkNotNull(httpSourceConfig.getListDeserializer());
            Preconditions.checkArgument((httpSourceConfig.getPort() > 0 && httpSourceConfig.getPort() < 65535));
            HttpServiceRegistryUpdater<T> registryUpdater = new HttpServiceRegistryUpdater<T>(httpSourceConfig);
            return buildFinder(httpSourceConfig, registryUpdater, shardSelector, nodeSelector, healthcheckRefreshTimeMillis);
        }
        if (curatorFrameworkConfig == null) {
            Preconditions.checkNotNull(curatorSourceConfig.getNamespace());
            Preconditions.checkNotNull(curatorSourceConfig.getConnectionString());
            Preconditions.checkNotNull(curatorSourceConfig.getDeserializer());
            Preconditions.checkNotNull(curatorSourceConfig.getServiceName());
            CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                    .namespace(curatorSourceConfig.getNamespace())
                    .connectString(curatorSourceConfig.getConnectionString())
                    .retryPolicy(new ExponentialBackoffRetry(1000, 100)).build();
            curatorFramework.start();
            CuratorServiceRegistryUpdater<T> registryUpdater = new CuratorServiceRegistryUpdater<T>(curatorSourceConfig.getDeserializer(), curatorFramework, curatorSourceConfig.getServiceName());
            return buildFinder(curatorSourceConfig, registryUpdater, shardSelector, nodeSelector, healthcheckRefreshTimeMillis);
        }
        Preconditions.checkNotNull(curatorFrameworkConfig.getCuratorFramework());
        Preconditions.checkNotNull(curatorFrameworkConfig.getDeserializer());
        Preconditions.checkNotNull(curatorFrameworkConfig.getServiceName());
        CuratorServiceRegistryUpdater<T> registryUpdater = new CuratorServiceRegistryUpdater<T>(curatorFrameworkConfig.getDeserializer(), curatorFrameworkConfig.getCuratorFramework(), curatorFrameworkConfig.getServiceName());
        return buildFinder(curatorFrameworkConfig, registryUpdater, shardSelector, nodeSelector, healthcheckRefreshTimeMillis);
    }

    protected abstract FinderType buildFinder(SourceConfig config,
                                              AbstractServiceRegistryUpdater<T> registryUpdater,
                                              ShardSelector<T, RegistryType> shardSelector,
                                              ServiceNodeSelector<T> nodeSelector,
                                              int healthcheckRefreshTimeMillis);

}
