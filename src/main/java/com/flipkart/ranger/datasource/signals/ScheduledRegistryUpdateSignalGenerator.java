package com.flipkart.ranger.datasource.signals;

import com.flipkart.ranger.finder.Service;
import com.flipkart.ranger.signals.ScheduledSignalGenerator;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

/**
 *
 */
@Slf4j
public class ScheduledRegistryUpdateSignalGenerator<T> extends ScheduledSignalGenerator<T> {

    public ScheduledRegistryUpdateSignalGenerator(
            Service service,
            long refreshIntervalMillis) {
        super(service, () -> null, Collections.emptyList(), refreshIntervalMillis);
    }

}
