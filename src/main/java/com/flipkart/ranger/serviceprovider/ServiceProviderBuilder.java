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
import com.flipkart.ranger.healthservice.ServiceHealthAggregator;
import com.flipkart.ranger.healthservice.monitor.IsolatedHealthMonitor;
import com.flipkart.ranger.model.Serializer;
import com.flipkart.ranger.model.ServiceNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ServiceProviderBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProviderBuilder.class);

    private String namespace;
    private String serviceName;
    private CuratorFramework curatorFramework;
    private String connectionString;
    private Serializer<T> serializer;
    private String hostname;
    private int port;
    private T nodeData;
    private int healthUpdateIntervalMs;
    private int staleUpdateThresholdMs;
    private List<Healthcheck> healthchecks = Lists.newArrayList();

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

    public ServiceProviderBuilder<T> withHealthUpdateIntervalMs(int healthUpdateIntervalMs) {
        this.healthUpdateIntervalMs = healthUpdateIntervalMs;
        return this;
    }

    public ServiceProviderBuilder<T> withStaleUpdateThresholdMs(int staleUpdateThresholdMs) {
        this.staleUpdateThresholdMs = staleUpdateThresholdMs;
        return this;
    }

    /**
     * Register a monitor to the service, to setup a continuous monitoring on the monitor
     * this method can be used to add a {@link IsolatedHealthMonitor} which will later be
     * scheduled at regular intervals and monitored to generate and maintain an aggregated health of the service
     * the scheduling will happen in an isolated thread
     *
     * @param monitor an implementation of the {@link IsolatedHealthMonitor}
     * @return builder for next call
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
        Preconditions.checkArgument(!healthchecks.isEmpty() || !isolatedMonitors.isEmpty());
        if (null == curatorFramework) {
            Preconditions.checkNotNull(connectionString);
            curatorFramework = CuratorFrameworkFactory.builder()
                    .namespace(namespace)
                    .connectString(connectionString)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 100)).build();
            curatorFramework.start();
        }
        if (healthUpdateIntervalMs < 1000 || healthUpdateIntervalMs > 60000) {
            LOGGER.warn("Health update interval should be between 3000ms and 60000ms. Current value: {} ms. " +
                "Being set to 3000ms", healthUpdateIntervalMs);
            healthUpdateIntervalMs = 3000;
        }
        if (staleUpdateThresholdMs < 1000 || staleUpdateThresholdMs > 45000) {
            LOGGER.warn("Stale update threshold should be between 1000ms and 45000ms. Current value: {} ms. " +
                "Being set to 45000ms", staleUpdateThresholdMs);
            staleUpdateThresholdMs = 45000;
        }
        final ServiceHealthAggregator serviceHealthAggregator = new ServiceHealthAggregator();
        for (IsolatedHealthMonitor isolatedMonitor : isolatedMonitors) {
            serviceHealthAggregator.addIsolatedMonitor(isolatedMonitor);
        }
        healthchecks.add(serviceHealthAggregator);
        return new ServiceProvider<>(serviceName, serializer, curatorFramework,
                                     new ServiceNode<>(hostname, port, nodeData), healthchecks,
                                     healthUpdateIntervalMs, staleUpdateThresholdMs, serviceHealthAggregator);
    }

}
