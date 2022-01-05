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
package com.flipkart.ranger.core.signals;

import com.flipkart.ranger.core.model.Service;
import lombok.Builder;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 */
@Slf4j
public class ScheduledSignal<T> extends Signal<T> {
    private final String name;
    private final long refreshIntervalMillis;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> scheduledFuture = null;

    @Builder
    public ScheduledSignal(
            final Service service,
            final Supplier<T> signalDataGenerator,
            @Singular List<Consumer<T>> consumers,
            long refreshIntervalMillis) {
        super(signalDataGenerator, consumers);
        this.name = String.format("timer-%s-%s", service.getNamespace(), service.getServiceName());
        this.refreshIntervalMillis = refreshIntervalMillis;
    }

    public ScheduledSignal(
            final String name,
            final Supplier<T> signalDataGenerator,
            @Singular List<Consumer<T>> consumers,
            long refreshIntervalMillis) {
        super(signalDataGenerator, consumers);
        this.name = name;
        this.refreshIntervalMillis = refreshIntervalMillis;
    }

    @Override
    public final void start() {
        scheduledFuture = scheduler.scheduleWithFixedDelay(() -> {
            try {
                onSignalReceived();
            } catch (Exception e) {
                log.error("Error delivering signal for:" + name , e);
            }
        }, 0, refreshIntervalMillis, TimeUnit.MILLISECONDS);
        log.info("Started scheduled signal generator: {}", name);
    }

    @Override
    public final void stop() {
        if(null != scheduledFuture) {
            scheduledFuture.cancel(true);
        }
        log.info("Stopped scheduled signal generator: {}", name);
    }
}
