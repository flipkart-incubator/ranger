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
package com.flipkart.ranger.core.healthservice.monitor;

import com.flipkart.ranger.core.healthcheck.HealthcheckStatus;

import java.io.File;

/**
 * Helper for creating instances of {@link Monitor}
 */
public class Monitors {

    private Monitors() {
        throw new InstantiationError("Must not instantiate this class");
    }

    public static Monitor<HealthcheckStatus> fileExistanceCheckMonitor(final String filePath) {
        return new Monitor<HealthcheckStatus>() {
            @Override
            public HealthcheckStatus monitor() {
                File file = new File(filePath);
                if (file.exists()) {
                    return HealthcheckStatus.healthy;
                } else {
                    return HealthcheckStatus.unhealthy;
                }
            }

            @Override
            public boolean isDisabled() {
                return false;
            }
        };
    }

    public static Monitor<HealthcheckStatus> defaultHealthyMonitor() {
        return new Monitor<HealthcheckStatus>() {
            @Override
            public HealthcheckStatus monitor() {
                return HealthcheckStatus.healthy;
            }

            @Override
            public boolean isDisabled() {
                return false;
            }
        };
    }
}
