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
package com.flipkart.ranger.core.finderhub;

import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceRegistry;
import com.flipkart.ranger.core.signals.ScheduledSignal;
import com.flipkart.ranger.core.signals.Signal;
import com.google.common.base.Preconditions;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 */
public abstract class ServiceFinderHubBuilder<T, C extends Criteria<T>, R extends ServiceRegistry<T>> {
    private ServiceDataSource serviceDataSource;
    private ServiceFinderFactory<T, C, R> serviceFinderFactory;
    private long refreshFrequencyMs = 10_000;
    private final List<Consumer<Void>> extraStartSignalConsumers = new ArrayList<>();
    private final List<Consumer<Void>> extraStopSignalConsumers = new ArrayList<>();
    private final List<Signal<Void>> extraRefreshSignals = new ArrayList<>();

    public ServiceFinderHubBuilder<T, C, R> withServiceDataSource(ServiceDataSource serviceDataSource) {
        this.serviceDataSource = serviceDataSource;
        return this;
    }

    public ServiceFinderHubBuilder<T, C, R> withServiceFinderFactory(ServiceFinderFactory<T,C, R> serviceFinderFactory) {
        this.serviceFinderFactory = serviceFinderFactory;
        return this;
    }
    
    public ServiceFinderHubBuilder<T, C, R> withRefreshFrequencyMs(long refreshFrequencyMs) {
        this.refreshFrequencyMs = refreshFrequencyMs;
        return this;
    }

    public ServiceFinderHubBuilder<T, C, R> withExtraStartSignalConsumer(Consumer<Void> consumer) {
        this.extraStartSignalConsumers.add(consumer);
        return this;
    }

    public ServiceFinderHubBuilder<T, C, R> withExtraStopSignalConsumer(Consumer<Void> consumer) {
        this.extraStopSignalConsumers.add(consumer);
        return this;
    }

    public ServiceFinderHubBuilder<T, C, R> withExtraRefreshSignal(Signal<Void> extraRefreshSignal) {
        this.extraRefreshSignals.add(extraRefreshSignal);
        return this;
    }

    public ServiceFinderHub<T, C, R> build() {
        preBuild();
        Preconditions.checkNotNull(serviceDataSource, "Provide a non-null service data source");
        Preconditions.checkNotNull(serviceFinderFactory, "Provide a non-null service finder factory");

        val hub = new ServiceFinderHub<>(serviceDataSource, serviceFinderFactory);
        final ScheduledSignal<Void> refreshSignal = new ScheduledSignal<>("service-hub-refresh-timer",
                                                                          () -> null,
                                                                          Collections.emptyList(),
                                                                          refreshFrequencyMs);
        hub.registerUpdateSignal(refreshSignal);
        extraRefreshSignals.forEach(hub::registerUpdateSignal);

        hub.getStartSignal()
                .registerConsumer(x -> serviceDataSource.start())
                .registerConsumer(x -> refreshSignal.start())
                .registerConsumers(extraStartSignalConsumers);
        hub.getStopSignal()
                .registerConsumers(extraStopSignalConsumers)
                .registerConsumer(x -> refreshSignal.stop())
                .registerConsumer(x -> serviceDataSource.stop());
        postBuild(hub);
        return hub;
    }

    protected abstract void preBuild();

    protected abstract void postBuild(ServiceFinderHub<T, C, R> serviceFinderHub);
}
