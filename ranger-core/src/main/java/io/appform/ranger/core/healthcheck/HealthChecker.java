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
package io.appform.ranger.core.healthcheck;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.function.Supplier;

/**
 * A Runnable which maintains the health state of the ServiceProvider
 */
@Slf4j
public class HealthChecker implements Supplier<HealthcheckResult> {

    private final List<Healthcheck> healthChecks;
    private final int staleUpdateThreshold;
    private HealthcheckStatus lastHealthcheckStatus;
    private long lastUpdatedTime;

    public HealthChecker(List<Healthcheck> healthChecks, int staleUpdateThreshold) {
        this.healthChecks = healthChecks;
        this.staleUpdateThreshold = staleUpdateThreshold;
    }

    @Override
    public HealthcheckResult get() {
        if(refreshHealth()) {
            return HealthcheckResult.builder()
                    .status(lastHealthcheckStatus)
                    .updatedTime(lastUpdatedTime)
                    .build();
        }
        return null;
    }

    private boolean refreshHealth() {
        HealthcheckStatus healthcheckStatus = HealthcheckStatus.unhealthy;
        for (Healthcheck healthcheck : healthChecks) {
            try {
                healthcheckStatus = healthcheck.check();
            }
            catch (Throwable t) {
                log.error("Error running healthcheck. Setting node to unhealthy", t);
                healthcheckStatus = HealthcheckStatus.unhealthy;
            }
            if (HealthcheckStatus.unhealthy == healthcheckStatus) {
                break;
            }
        }
        //Trigger update only if state change has happened
        //Conditions on which update will be triggered
        //1. First time
        //2. Stale update threshold breach
        //3. Update in health status
        try {
            val currentTime = System.currentTimeMillis();
            if (lastHealthcheckStatus == null
                    || (currentTime - lastUpdatedTime) > staleUpdateThreshold
                    || lastHealthcheckStatus != healthcheckStatus) {
                lastUpdatedTime = currentTime;
                return true;
            }
        }
        finally {
            lastHealthcheckStatus = healthcheckStatus;
        }
        return false;
    }
}
