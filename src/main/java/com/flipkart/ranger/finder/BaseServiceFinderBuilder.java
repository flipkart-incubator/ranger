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

public abstract class BaseServiceFinderBuilder<T, RegistryType extends ServiceRegistry<T>, FinderType extends ServiceFinder<T, RegistryType>> {
    private SourceConfig sourceConfig;
    private ServiceRegistryUpdater<T> serviceRegistryUpdater;

    private int healthcheckRefreshTimeMillis;
    private ShardSelector<T, RegistryType> shardSelector;
    private ServiceNodeSelector<T> nodeSelector = new RandomServiceNodeSelector<T>();

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withSourceConfig(SourceConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
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

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withHealthcheckRefreshTimeMillis(int healthcheckRefreshTimeMillis) {
        this.healthcheckRefreshTimeMillis = healthcheckRefreshTimeMillis;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withServiceRegistryUpdater(ServiceRegistryUpdater serviceRegistryUpdater) {
        this.serviceRegistryUpdater = serviceRegistryUpdater;
        return this;
    }

    public FinderType build() throws Exception {
        if (0 == healthcheckRefreshTimeMillis) {
            healthcheckRefreshTimeMillis = 1000;
        }

        if(serviceRegistryUpdater == null) {
            ServiceRegistryUpdaterFactory<T> serviceRegistryUpdaterFactory = new ServiceRegistryUpdaterFactory<T>();
            serviceRegistryUpdater = serviceRegistryUpdaterFactory.getServiceRegistryUpdater(sourceConfig);
        }
        return buildFinder(sourceConfig, serviceRegistryUpdater, shardSelector, nodeSelector, healthcheckRefreshTimeMillis);
    }

    protected abstract FinderType buildFinder(SourceConfig config,
                                              ServiceRegistryUpdater<T> registryUpdater,
                                              ShardSelector<T, RegistryType> shardSelector,
                                              ServiceNodeSelector<T> nodeSelector,
                                              int healthcheckRefreshTimeMillis);

}
