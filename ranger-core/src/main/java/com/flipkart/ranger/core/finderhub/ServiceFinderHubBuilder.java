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
public abstract class ServiceFinderHubBuilder<T, R extends ServiceRegistry<T>, U extends Criteria<T, R>> {
    private ServiceDataSource serviceDataSource;
    private ServiceFinderFactory<T, R, U> serviceFinderFactory;
    private long refreshFrequencyMs = 10_000;
    private List<Consumer<Void>> extraStartSignalConsumers = new ArrayList<>();
    private List<Consumer<Void>> extraStopSignalConsumers = new ArrayList<>();
    private List<Signal<Void>> extraRefreshSignals = new ArrayList<>();

    public ServiceFinderHubBuilder<T, R, U> withServiceDataSource(ServiceDataSource serviceDataSource) {
        this.serviceDataSource = serviceDataSource;
        return this;
    }

    public ServiceFinderHubBuilder<T, R, U> withServiceFinderFactory(ServiceFinderFactory<T, R, U> serviceFinderFactory) {
        this.serviceFinderFactory = serviceFinderFactory;
        return this;
    }
    
    public ServiceFinderHubBuilder<T, R, U> withRefreshFrequencyMs(long refreshFrequencyMs) {
        this.refreshFrequencyMs = refreshFrequencyMs;
        return this;
    }

    public ServiceFinderHubBuilder<T, R, U> withExtraStartSignalConsumer(Consumer<Void> consumer) {
        this.extraStartSignalConsumers.add(consumer);
        return this;
    }

    public ServiceFinderHubBuilder<T, R, U> withExtraStopSignalConsumer(Consumer<Void> consumer) {
        this.extraStopSignalConsumers.add(consumer);
        return this;
    }

    public ServiceFinderHubBuilder<T, R, U> withExtraRefreshSignal(Signal<Void> extraRefreshSignal) {
        this.extraRefreshSignals.add(extraRefreshSignal);
        return this;
    }

    public ServiceFinderHub<T, R, U> build() {
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
    protected abstract void postBuild(ServiceFinderHub<T,R, U> serviceFinderHub);
}
