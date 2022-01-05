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
package io.appform.ranger.core.signals;

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
