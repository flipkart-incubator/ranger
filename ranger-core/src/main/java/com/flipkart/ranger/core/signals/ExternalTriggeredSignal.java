package com.flipkart.ranger.core.signals;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 */
public class ExternalTriggeredSignal<T> extends Signal<T> {

     public ExternalTriggeredSignal(
            Supplier<T> signalDataGenerator,
            List<Consumer<T>> consumers) {
        super(signalDataGenerator, consumers);
    }

    public void trigger() {
        super.onSignalReceived();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
