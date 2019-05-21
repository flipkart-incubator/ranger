package com.flipkart.ranger.healthcheck;

import java.io.File;

/**
 *
 */
public class Healthchecks {
    private Healthchecks() {
        throw new InstantiationError("Must not instantiate this class");
    }

    public static Healthcheck fileExistanceCheck(final String filePath) {
        return () -> {
            File file = new File(filePath);
            if (file.exists()) {
                return HealthcheckStatus.healthy;
            } else {
                return HealthcheckStatus.unhealthy;
            }
        };
    }

    public static Healthcheck defaultHealthyCheck() {
        return () -> HealthcheckStatus.healthy;
    }
}
