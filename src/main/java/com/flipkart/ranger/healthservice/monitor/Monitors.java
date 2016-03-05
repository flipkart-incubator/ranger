package com.flipkart.ranger.healthservice.monitor;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;

import java.io.File;

/**
 * @author tushar.naik
 * @version 1.0
 * @date 28/02/16 - 1:07 AM
 */
public class Monitors {

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
}
