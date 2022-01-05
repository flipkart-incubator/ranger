/*
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
package com.flipkart.ranger.zookeeper.serviceprovider;

import com.flipkart.ranger.core.model.NodeDataSink;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.serviceprovider.BaseServiceProviderBuilder;
import com.flipkart.ranger.core.serviceprovider.ServiceProvider;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataSerializer;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 *
 */
@Slf4j
public class ZkServiceProviderBuilder<T> extends BaseServiceProviderBuilder<T, ZkServiceProviderBuilder<T>, ZkNodeDataSerializer<T>> {
    private CuratorFramework curatorFramework;
    private String connectionString;

    public ZkServiceProviderBuilder<T> withCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
        return this;
    }

    public ZkServiceProviderBuilder<T> withConnectionString(final String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    @Override
    public ServiceProvider<T, ZkNodeDataSerializer<T>> build() {
        if (null == curatorFramework) {
            Preconditions.checkNotNull(connectionString);
            log.info("Building custom curator framework");
            curatorFramework = CuratorFrameworkFactory.builder()
                    .namespace(namespace)
                    .connectString(connectionString)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 100)).build();
            super.withStartSignalHandler(x -> curatorFramework.start());
            super.withStopSignalHandler(x -> curatorFramework.close());
        }
        return super.buildProvider();
    }

    @Override
    protected NodeDataSink<T, ZkNodeDataSerializer<T>> dataSink(Service service) {
        return new ZkNodeDataSink<>(service, curatorFramework);
    }
}
