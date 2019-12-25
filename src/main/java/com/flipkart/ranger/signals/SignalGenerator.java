package com.flipkart.ranger.signals;

import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 */
public abstract class SignalGenerator<T> {

    private final Supplier<T> signalDataGenerator;
    private final List<Consumer<T>> consumers = new ArrayList<>();

    protected SignalGenerator(Supplier<T> signalDataGenerator, List<Consumer<T>> consumers) {
        this.signalDataGenerator = signalDataGenerator;
        if(null != consumers) {
            this.consumers.addAll(consumers);
        }
    }

    protected void onSignalReceived() {
        val signalData = signalDataGenerator.get();
        consumers.forEach(consumer -> consumer.accept(signalData));
    }

    public void registerConsumer(Consumer<T> consumer) {
        consumers.add(consumer);
    }

    public abstract void start();
    public abstract void stop();
}
