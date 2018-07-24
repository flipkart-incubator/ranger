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
import com.google.common.base.Strings;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;

public abstract class BaseServiceFinderBuilder<T, RegistryType extends ServiceRegistry<T>, FinderType extends ServiceFinder<T, RegistryType>> {
    private String namespace;
    private String serviceName;
    private CuratorFramework curatorFramework;
    private String host;
    private Integer port;
    private String path;
    private String connectionString;
    private int healthcheckRefreshTimeMillis;
    private Deserializer<T> deserializer;
    private ShardSelector<T, RegistryType> shardSelector;
    private ServiceNodeSelector<T> nodeSelector = new RandomServiceNodeSelector<T>();

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

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withHost(final String host) {
        this.host = host;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withPort(final int port) {
        this.port = port;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withPath(final String path) {
        this.path = path;
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

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> witHhealthcheckRefreshTimeMillis(int healthcheckRefreshTimeMillis) {
        this.healthcheckRefreshTimeMillis = healthcheckRefreshTimeMillis;
        return this;
    }

    public FinderType build() throws Exception {
        //TODO: check conditions properly
        if( 0 == healthcheckRefreshTimeMillis) {
            healthcheckRefreshTimeMillis = 1000;
        }
        Service service;
        if (null == curatorFramework && !Strings.isNullOrEmpty(connectionString)) {
            Preconditions.checkNotNull(namespace);
            Preconditions.checkNotNull(serviceName);
            Preconditions.checkNotNull(deserializer);
            service = buildCuratorFrameworkService(connectionString, namespace, serviceName);
            return buildFinder(service, deserializer, shardSelector, nodeSelector, healthcheckRefreshTimeMillis);
        }
        if ((!Strings.isNullOrEmpty(host))){
            Preconditions.checkNotNull(host);
            Preconditions.checkNotNull(port);
            Preconditions.checkNotNull(path);
            service = buildHttpService(host, port, path);
            return buildFinder(service, deserializer, shardSelector, nodeSelector, healthcheckRefreshTimeMillis);
        }
        if(null != curatorFramework){
            curatorFramework.start();
            service = new CuratorService(curatorFramework, namespace, serviceName);
            return buildFinder(service, deserializer, shardSelector, nodeSelector, healthcheckRefreshTimeMillis);
        }
        //TODO: what should be the default case?
        service = new CuratorService(curatorFramework, namespace, serviceName);
        return buildFinder(service, deserializer, shardSelector, nodeSelector, healthcheckRefreshTimeMillis);
    }

    private Service buildCuratorFrameworkService(String connectionString, String namespace, String serviceName) throws Exception {
        curatorFramework = CuratorFrameworkFactory.builder()
                .namespace(namespace)
                .connectString(connectionString)
                .retryPolicy(new ExponentialBackoffRetry(1000, 100)).build();
        curatorFramework.start();
        CuratorService curatorService = new CuratorService(curatorFramework, namespace, serviceName);
        return curatorService;
    }

    private Service buildHttpService(String host, int port, String path) throws Exception{
        URI uri = new URIBuilder()
                .setScheme("http")
                .setHost(host)
                .setPort(port)
                .setPath(path)
                .build();
        return new HttpService(uri);
    }

    protected abstract FinderType buildFinder(Service service,
                                              Deserializer<T> deserializer,
                                              ShardSelector<T, RegistryType> shardSelector,
                                              ServiceNodeSelector<T> nodeSelector,
                                              int healthcheckRefreshTimeMillis);

}
