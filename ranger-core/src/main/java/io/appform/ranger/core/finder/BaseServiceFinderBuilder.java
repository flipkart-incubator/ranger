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

package io.appform.ranger.core.finder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.appform.ranger.core.finder.nodeselector.RandomServiceNodeSelector;
import io.appform.ranger.core.finder.serviceregistry.ServiceRegistryUpdater;
import io.appform.ranger.core.finder.serviceregistry.signal.ScheduledRegistryUpdateSignal;
import io.appform.ranger.core.model.*;
import io.appform.ranger.core.signals.Signal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@SuppressWarnings("unchecked")
public abstract class BaseServiceFinderBuilder
        <
                T,
                R extends ServiceRegistry<T>,
                F extends ServiceFinder<T, R>,
                B extends BaseServiceFinderBuilder<T, R, F, B, D>,
                D extends Deserializer<T>> {

    protected String namespace;
    protected String serviceName;
    protected int nodeRefreshIntervalMs;
    protected boolean disablePushUpdaters;
    protected D deserializer;
    protected ShardSelector<T, R> shardSelector;
    protected ServiceNodeSelector<T> nodeSelector = new RandomServiceNodeSelector<>();
    protected final List<Signal<T>> additionalRefreshSignals = new ArrayList<>();
    protected final List<Consumer<Void>> startSignalHandlers = Lists.newArrayList();
    protected final List<Consumer<Void>> stopSignalHandlers = Lists.newArrayList();

    public B withNamespace(final String namespace) {
        this.namespace = namespace;
        return (B)this;
    }

    public B withServiceName(final String serviceName) {
        this.serviceName = serviceName;
        return (B)this;
    }

    public B withDeserializer(D deserializer) {
        this.deserializer = deserializer;
        return (B)this;
    }

    public B withShardSelector(ShardSelector<T, R> shardSelector) {
        this.shardSelector = shardSelector;
        return (B)this;
    }

    public B withNodeSelector(ServiceNodeSelector<T> nodeSelector) {
        this.nodeSelector = null != nodeSelector ? nodeSelector : this.nodeSelector;
        return (B)this;
    }

    public B withNodeRefreshIntervalMs(int nodeRefreshIntervalMs) {
        this.nodeRefreshIntervalMs = nodeRefreshIntervalMs;
        return (B)this;
    }

    public B withDisableWatchers() {
        this.disablePushUpdaters = true;
        return (B)this;
    }

    public B withDisableWatchers(boolean disablePushUpdaters) {
        this.disablePushUpdaters = disablePushUpdaters;
        return (B)this;
    }

    public B withAdditionalSignalGenerator(Signal<T> signalGenerator) {
        this.additionalRefreshSignals.add(signalGenerator);
        return (B)this;
    }

    public B withAdditionalSignalGenerators(Signal<T>... signalGenerators) {
        this.additionalRefreshSignals.addAll(Arrays.asList(signalGenerators));
        return (B)this;
    }

    public B withAdditionalSignalGenerators(List<Signal<T>> signalGenerators) {
        this.additionalRefreshSignals.addAll(signalGenerators);
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

    public abstract F build();

    protected F buildFinder() {
        Preconditions.checkNotNull(namespace);
        Preconditions.checkNotNull(serviceName);
        Preconditions.checkNotNull(deserializer);

        if (nodeRefreshIntervalMs < 1000) {
            log.warn("Node refresh interval for {} is too low: {} ms. Has been upgraded to 1000ms ",
                     serviceName, nodeRefreshIntervalMs);
            nodeRefreshIntervalMs = 1000;
        }
        val service = Service.builder().namespace(namespace).serviceName(serviceName).build();
        val finder = buildFinder(service, shardSelector, nodeSelector);
        val registry = finder.getServiceRegistry();
        val signalGenerators = new ArrayList<Signal<T>>();
        val nodeDataSource = dataSource(service);

        signalGenerators.add(new ScheduledRegistryUpdateSignal<>(service, nodeRefreshIntervalMs));
        additionalRefreshSignals.addAll(implementationSpecificRefreshSignals(service, nodeDataSource));
        if (!additionalRefreshSignals.isEmpty()) {
            signalGenerators.addAll(additionalRefreshSignals);
            log.debug("Added additional signal handlers");
        }

        val updater = new ServiceRegistryUpdater<>(registry, nodeDataSource, signalGenerators, deserializer);
        finder.getStartSignal()
                .registerConsumers(startSignalHandlers)
                .registerConsumer(x -> nodeDataSource.start())
                .registerConsumer(x -> updater.start())
                .registerConsumer(x -> signalGenerators.forEach(Signal::start));

        finder.getStopSignal()
                .registerConsumer(x -> signalGenerators.forEach(Signal::stop))
                .registerConsumer(x -> updater.stop())
                .registerConsumer(x -> nodeDataSource.stop())
                .registerConsumers(stopSignalHandlers);
        return finder;
    }

    @SuppressWarnings("unused")
    protected List<Signal<T>> implementationSpecificRefreshSignals(Service service, NodeDataSource<T, D> nodeDataSource) {
        return Collections.emptyList();
    }

    protected abstract NodeDataSource<T, D> dataSource(Service service);

    protected abstract F buildFinder(
            Service service,
            ShardSelector<T, R> shardSelector,
            ServiceNodeSelector<T> nodeSelector);

}
