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

package io.appform.ranger.core.serviceprovider;

import io.appform.ranger.core.healthcheck.HealthChecker;
import io.appform.ranger.core.healthcheck.Healthcheck;
import io.appform.ranger.core.healthcheck.HealthcheckResult;
import io.appform.ranger.core.healthcheck.HealthcheckStatus;
import io.appform.ranger.core.healthservice.HealthService;
import io.appform.ranger.core.healthservice.ServiceHealthAggregator;
import io.appform.ranger.core.healthservice.monitor.IsolatedHealthMonitor;
import io.appform.ranger.core.model.NodeDataSink;
import io.appform.ranger.core.model.Serializer;
import io.appform.ranger.core.model.Service;
import io.appform.ranger.core.model.ServiceNode;
import io.appform.ranger.core.signals.ScheduledSignal;
import io.appform.ranger.core.signals.Signal;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseServiceProviderBuilder<T, B extends BaseServiceProviderBuilder<T, B, S>, S extends Serializer<T>> {

    protected String namespace;
    protected String serviceName;
    protected S serializer;
    protected String hostname;
    protected int port;
    protected T nodeData;
    protected int healthUpdateIntervalMs;
    protected int staleUpdateThresholdMs;
    protected NodeDataSink<T, S> nodeDataSource = null;
    protected List<Healthcheck> healthchecks = Lists.newArrayList();
    protected final List<Consumer<Void>> startSignalHandlers = Lists.newArrayList();
    protected final List<Consumer<Void>> stopSignalHandlers = Lists.newArrayList();
    protected final List<Signal<HealthcheckResult>> additionalRefreshSignals = Lists.newArrayList();

    /* list of isolated monitors */
    private final List<IsolatedHealthMonitor<HealthcheckStatus>> isolatedMonitors = Lists.newArrayList();

    public BaseServiceProviderBuilder<T, B, S> withNamespace(final String namespace) {
        this.namespace = namespace;
        return this;
    }

    public B withServiceName(final String serviceName) {
        this.serviceName = serviceName;
        return (B)this;
    }

    public B withSerializer(S serializer) {
        this.serializer = serializer;
        return (B)this;
    }

    public B withHostname(final String hostname) {
        this.hostname = hostname;
        return (B)this;
    }

    public B withPort(int port) {
        this.port = port;
        return (B)this;
    }

    public B withNodeData(T nodeData) {
        this.nodeData = nodeData;
        return (B)this;
    }

    public B withHealthcheck(Healthcheck healthcheck) {
        this.healthchecks.add(healthcheck);
        return (B)this;
    }

    public B withHealthUpdateIntervalMs(int healthUpdateIntervalMs) {
        this.healthUpdateIntervalMs = healthUpdateIntervalMs;
        return (B)this;
    }

    public B withStaleUpdateThresholdMs(int staleUpdateThresholdMs) {
        this.staleUpdateThresholdMs = staleUpdateThresholdMs;
        return (B)this;
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
    public B withIsolatedHealthMonitor(IsolatedHealthMonitor<HealthcheckStatus> monitor) {
        this.isolatedMonitors.add(monitor);
        return (B)this;
    }

    public B withNodeDataSource(NodeDataSink<T, S> nodeDataSource) {
        this.nodeDataSource = nodeDataSource;
        return (B)this;
    }

    public B withStartSignalHandler(Consumer<Void> startSignalHandler) {
        this.startSignalHandlers.add(startSignalHandler);
        return (B)this;
    }

    public B withStartSignalHandlers(List<Consumer<Void>> startSignalHandlers) {
        this.startSignalHandlers.addAll(startSignalHandlers);
        return (B)this;
    }

    public B withStopSignalHandler(Consumer<Void> stopSignalHandler) {
        this.stopSignalHandlers.add(stopSignalHandler);
        return (B)this;
    }

    public B withStopSignalHandlers(List<Consumer<Void>> stopSignalHandlers) {
        this.stopSignalHandlers.addAll(stopSignalHandlers);
        return (B)this;
    }

    public B withAdditionalRefreshSignal(Signal<HealthcheckResult> additionalRefreshSignal) {
        this.additionalRefreshSignals.add(additionalRefreshSignal);
        return (B)this;
    }

    public B withAdditionalRefreshSignals(List<Signal<HealthcheckResult>> additionalRefreshSignals) {
        this.additionalRefreshSignals.addAll(additionalRefreshSignals);
        return (B)this;
    }

    protected final ServiceProvider<T, S> buildProvider() {
        Preconditions.checkNotNull(namespace);
        Preconditions.checkNotNull(serviceName);
        Preconditions.checkNotNull(serializer);
        Preconditions.checkNotNull(hostname);
        Preconditions.checkArgument(port > 0);
        Preconditions.checkArgument(!healthchecks.isEmpty() || !isolatedMonitors.isEmpty());

        if (healthUpdateIntervalMs < 1000 || healthUpdateIntervalMs > 20000) {
            log.warn("Health update interval for {} should be between 1000ms and 20000ms. Current value: {} ms. " +
                             "Being set to 1000ms", serviceName, healthUpdateIntervalMs);
            healthUpdateIntervalMs = 1000;
        }

        if (staleUpdateThresholdMs < 5000 || staleUpdateThresholdMs > 20000) {
            log.warn("Stale update threshold for {} should be between 5000ms and 20000ms. Current value: {} ms. " +
                             "Being set to 5000ms", serviceName, staleUpdateThresholdMs);
            staleUpdateThresholdMs = 5000;
        }

        val serviceHealthAggregator = new ServiceHealthAggregator();
        isolatedMonitors.forEach(serviceHealthAggregator::addIsolatedMonitor);

        healthchecks.add(serviceHealthAggregator);
        val service = Service.builder().namespace(namespace).serviceName(serviceName).build();
        val usableNodeDataSource = dataSink(service);

        val healthcheckUpdateSignalGenerator
                = new ScheduledSignal<>(
                service,
                new HealthChecker(healthchecks, staleUpdateThresholdMs),
                Collections.emptyList(),
                healthUpdateIntervalMs
        );

        val healthServices = Collections.singletonList(serviceHealthAggregator);

        val signalGenerators
                = ImmutableList.<Signal<HealthcheckResult>>builder()
                .add(healthcheckUpdateSignalGenerator)
                .addAll(additionalRefreshSignals)
                .build();
        val serviceProvider = new ServiceProvider<>(service, ServiceNode.<T>builder().host(hostname).port(port).nodeData(nodeData).build(),
                                                    serializer,
                                                    usableNodeDataSource,
                                                    signalGenerators);
        val startSignal = serviceProvider.getStartSignal();

        startSignal
                .registerConsumers(startSignalHandlers)
                .registerConsumer(x -> usableNodeDataSource.start())
                .registerConsumer(x -> healthServices.forEach(HealthService::start))
                .registerConsumer(x -> signalGenerators.forEach(Signal::start));

        val stopSignal = serviceProvider.getStopSignal();
        stopSignal
                .registerConsumer(x -> healthServices.forEach(HealthService::stop))
                .registerConsumer(x -> signalGenerators.forEach(Signal::stop))
                .registerConsumer(x -> usableNodeDataSource.stop())
                .registerConsumers(stopSignalHandlers);
        return serviceProvider;
    }

    public abstract ServiceProvider<T,S> build();

    protected abstract NodeDataSink<T,S> dataSink(final Service service);
}
