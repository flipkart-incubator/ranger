package com.flipkart.ranger.core.finder.signals;

import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.signals.ScheduledSignal;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

/**
 *
 */
@Slf4j
public class ScheduledRegistryUpdateSignal<T> extends ScheduledSignal<T> {

    public ScheduledRegistryUpdateSignal(
            Service service,
            long refreshIntervalMillis) {
        super(service, () -> null, Collections.emptyList(), refreshIntervalMillis);
    }

}
