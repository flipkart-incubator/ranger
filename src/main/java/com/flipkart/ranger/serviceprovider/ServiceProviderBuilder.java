/**
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.ranger.serviceprovider;

import com.flipkart.ranger.healthcheck.Healthcheck;
import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.healthservice.ServiceHealthAggregator;
import com.flipkart.ranger.healthservice.monitor.IsolatedHealthMonitor;
import com.flipkart.ranger.healthservice.monitor.Monitor;
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
    private int refreshIntervalMillis;
    private List<Healthcheck> healthchecks = Lists.newArrayList();

    /* list of inline monitors */
    private List<Monitor<HealthcheckStatus>> inlineMonitors = Lists.newArrayList();

    /* list of isolated monitors */
    private List<IsolatedHealthMonitor> isolatedMonitors = Lists.newArrayList();

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

    public ServiceProviderBuilder<T> withRefreshIntervalMillis(int refreshIntervalMillis) {
        this.refreshIntervalMillis = refreshIntervalMillis;
        return this;
    }

    /**
     * Add a monitor which will be run in the same thread.
     * <p/>
     * this method can be used to add a {@link Monitor}
     * this monitor will not be scheduled in a separate isolated thread,
     * but instead its execution will happen inline, in a single thread, along with other inline monitors
     *
     * @param monitor an implementation of line {@link Monitor<HealthcheckStatus>}
     */
    public ServiceProviderBuilder<T> withInlineHealthMonitor(Monitor<HealthcheckStatus> monitor) {
        this.inlineMonitors.add(monitor);
        return this;
    }

    /**
     * Register a monitor to the service, to setup a continuous monitoring on the monitor
     * <p/>
     * this method can be used to add a {@link IsolatedHealthMonitor} which will later be
     * scheduled at regular intervals and monitored to generate and maintain an aggregated health of the service
     * the scheduling will happen in an isolated thread
     *
     * @param monitor an implementation of the {@link IsolatedHealthMonitor}
     */
    public ServiceProviderBuilder<T> withIsolatedHealthMonitor(IsolatedHealthMonitor monitor) {
        this.isolatedMonitors.add(monitor);
        return this;
    }

    public ServiceProvider<T> buildServiceDiscovery() {
        Preconditions.checkNotNull(namespace);
        Preconditions.checkNotNull(serviceName);
        Preconditions.checkNotNull(serializer);
        Preconditions.checkNotNull(hostname);
        Preconditions.checkArgument(port > 0);
        Preconditions.checkArgument(!healthchecks.isEmpty());
        if (null == curatorFramework) {
            Preconditions.checkNotNull(connectionString);
            curatorFramework = CuratorFrameworkFactory.builder()
                    .namespace(namespace)
                    .connectString(connectionString)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 100)).build();
            curatorFramework.start();
        }
        if (0 == refreshIntervalMillis) {
            refreshIntervalMillis = 1000;
        }
        final ServiceHealthAggregator serviceHealthAggregator = new ServiceHealthAggregator();
        for (Monitor<HealthcheckStatus> inlineMonitor : inlineMonitors) {
            serviceHealthAggregator.addInlineMonitor(inlineMonitor);
        }
        for (IsolatedHealthMonitor isolatedMonitor : isolatedMonitors) {
            serviceHealthAggregator.addIsolatedMonitor(isolatedMonitor);
        }
        healthchecks.add(serviceHealthAggregator);
        return new ServiceProvider<T>(serviceName, serializer, curatorFramework,
                new ServiceNode<T>(hostname, port, nodeData), healthchecks, refreshIntervalMillis, serviceHealthAggregator);
    }

}
