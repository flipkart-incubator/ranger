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
    private CuratorFramework curatorFramework;
    private CuratorSourceConfig curatorConfig;
    private HttpSourceConfig httpConfig;
    private String serviceName;

    private int healthcheckRefreshTimeMillis;
    private Deserializer<T> deserializer;
    private ShardSelector<T, RegistryType> shardSelector;
    private ServiceNodeSelector<T> nodeSelector = new RandomServiceNodeSelector<T>();

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withHttpSourceConfig(HttpSourceConfig httpConfig) {
        this.httpConfig = httpConfig;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withCuratorSourceConfig(CuratorSourceConfig curatorConfig) {
        this.curatorConfig = curatorConfig;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withServiceName(final String serviceName) {
        this.serviceName = serviceName;
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

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> witHhealthcheckRefreshTimeMillis(int healthcheckRefreshTimeMillis) {
        this.healthcheckRefreshTimeMillis = healthcheckRefreshTimeMillis;
        return this;
    }

    public FinderType build() throws Exception {
        if( 0 == healthcheckRefreshTimeMillis) {
            healthcheckRefreshTimeMillis = 1000;
        }
        if (curatorFramework != null) {
            CuratorSourceConfig curatorConfig = new CuratorSourceConfig(null,curatorFramework.getNamespace());
            CuratorServiceRegistryUpdater<T> registryUpdater = new CuratorServiceRegistryUpdater<T>(curatorConfig, deserializer, curatorFramework, serviceName);
            return buildFinder(curatorConfig, registryUpdater, deserializer, shardSelector, nodeSelector, healthcheckRefreshTimeMillis);
        }
        if (null != httpConfig){
            Preconditions.checkNotNull(deserializer);
            HttpServiceRegistryUpdater<T> registryUpdater = new HttpServiceRegistryUpdater<T>(httpConfig, deserializer);
            return buildFinder(httpConfig, registryUpdater, deserializer, shardSelector, nodeSelector, healthcheckRefreshTimeMillis);
        }
        if (null != curatorConfig) {
            Preconditions.checkNotNull(deserializer);
            CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                                                .namespace(curatorConfig.getNamespace())
                                                .connectString(curatorConfig.getConnectionString())
                                                .retryPolicy(new ExponentialBackoffRetry(1000, 100)).build();
            CuratorServiceRegistryUpdater<T> registryUpdater = new CuratorServiceRegistryUpdater<T>(curatorConfig, deserializer, curatorFramework, serviceName);
            return buildFinder(curatorConfig, registryUpdater, deserializer, shardSelector, nodeSelector, healthcheckRefreshTimeMillis);
        }
        //TODO: what should be the default case?
        return buildFinder(null, null, deserializer, shardSelector, nodeSelector, healthcheckRefreshTimeMillis);
    }

    protected abstract FinderType buildFinder(SourceConfig config,
                                              AbstractServiceRegistryUpdater<T> registryUpdater,
                                              Deserializer<T> deserializer,
                                              ShardSelector<T, RegistryType> shardSelector,
                                              ServiceNodeSelector<T> nodeSelector,
                                              int healthcheckRefreshTimeMillis);

}
