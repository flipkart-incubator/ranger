package com.flipkart.ranger.healthcheck;

import java.io.File;

/**
 * @author tushar.naik
 * @version 1.0
 * @date 09/05/16 - 12:21 AM
 */
public class Healthchecks {
    private Healthchecks() {
        throw new InstantiationError("Must not instantiate this class");
    }

    public static Healthcheck fileExistanceCheck(final String filePath) {
        return new Healthcheck() {
            @Override
            public HealthcheckStatus check() {
                File file = new File(filePath);
                if (file.exists()) {
                    return HealthcheckStatus.healthy;
                } else {
                    return HealthcheckStatus.unhealthy;
                }
            }
        };
    }

    public static Healthcheck defaultHealthyCheck() {
        return new Healthcheck() {
            @Override
            public HealthcheckStatus check() {
                return HealthcheckStatus.healthy;
            }
        };
    }
}
