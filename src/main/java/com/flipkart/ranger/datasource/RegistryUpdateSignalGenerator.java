package com.flipkart.ranger.datasource;

import com.flipkart.ranger.finder.Service;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class RegistryUpdateSignalGenerator<T> {
    private final Service service;
    private final NodeDataSource<T> dataSource;
    private final List<Runnable> consumers = new ArrayList<>();

    protected RegistryUpdateSignalGenerator(Service service, NodeDataSource<T> dataSource) {
        this.service = service;
        this.dataSource = dataSource;
    }

    public abstract void start();
    public abstract void shutdown();

    public void registerConsumer(Runnable r) {
        consumers.add(r);
    }

    protected final void onSignalReceived() {
        consumers.forEach(Runnable::run);
    }
}
