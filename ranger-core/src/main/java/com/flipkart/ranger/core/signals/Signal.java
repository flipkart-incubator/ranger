package com.flipkart.ranger.core.signals;

import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 */
public abstract class Signal<T> {

    private final Supplier<T> signalDataGenerator;
    private final List<Consumer<T>> consumers = new ArrayList<>();

    protected Signal(Supplier<T> signalDataGenerator, List<Consumer<T>> consumers) {
        this.signalDataGenerator = signalDataGenerator;
        if(null != consumers) {
            this.consumers.addAll(consumers);
        }
    }

    protected void onSignalReceived() {
        val signalData = signalDataGenerator.get();
        consumers.forEach(consumer -> consumer.accept(signalData));
    }

    public Signal<T> registerConsumer(Consumer<T> consumer) {
        consumers.add(consumer);
        return this;
    }

    public Signal<T> registerConsumers(List<Consumer<T>> consumers) {
        this.consumers.addAll(consumers);
        return this;
    }

    public abstract void start();
    public abstract void stop();
}
