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
package com.flipkart.ranger.client.zk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.client.AbstractHubClient;
import com.flipkart.ranger.core.finderhub.ServiceDataSource;
import com.flipkart.ranger.core.finderhub.ServiceFinderHub;
import com.flipkart.ranger.core.finderhub.StaticDataSource;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceRegistry;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataDeserializer;
import com.flipkart.ranger.zookeeper.servicefinderhub.ZkServiceDataSource;
import com.flipkart.ranger.zookeeper.servicefinderhub.ZkServiceFinderHubBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;

import java.util.List;

@Slf4j
@Getter
public abstract class AbstractZKHubClient<T, C extends Criteria<T>, R extends ServiceRegistry<T>, D extends ZkNodeDataDeserializer<T>> extends AbstractHubClient<T, C, R, D> {

    private final boolean disablePushUpdaters;
    private final String connectionString;
    private final CuratorFramework curatorFramework;
    /*
        Use this if you don't want the datasource to fetch the entire list of services;
         but know beforehand about the services you are going to build the hub for.
    */
    private final List<Service> services;

    public AbstractZKHubClient(
            String namespace,
            ObjectMapper mapper,
            int refreshTimeMs,
            boolean disablePushUpdaters,
            String connectionString,
            CuratorFramework curatorFramework,
            C criteria,
            D deserializer,
            List<Service> services
    ) {
        super(namespace, mapper, refreshTimeMs, criteria, deserializer);
        this.disablePushUpdaters = disablePushUpdaters;
        this.connectionString = connectionString;
        this.services = services;

        this.curatorFramework = null != curatorFramework ? curatorFramework :
                CuratorFrameworkFactory.builder()
                        .namespace(namespace)
                        .connectString(this.connectionString)
                        .retryPolicy(new RetryForever(5000))
                        .build();

    }

    @Override
    protected ServiceFinderHub<T,C, R> buildHub() {
       return new ZkServiceFinderHubBuilder<T, C, R>()
                .withCuratorFramework(curatorFramework)
                .withConnectionString(connectionString)
                .withNamespace(getNamespace())
                .withRefreshFrequencyMs(getRefreshTimeMs())
                .withServiceDataSource(buildServiceDataSource())
                .withServiceFinderFactory(buildFinderFactory())
                .build();
    }

    @Override
    protected ServiceDataSource buildServiceDataSource() {
        return null != services && !services.isEmpty() ?
                new StaticDataSource(services) :
                new ZkServiceDataSource(getNamespace(), connectionString, curatorFramework);
    }

}

