package com.flipkart.ranger.healthcheck;

import lombok.Builder;
import lombok.Data;

/**
 *
 */
@Data
@Builder
public class HealthcheckResult {
    private final HealthcheckStatus status;
    private final long updatedTime;
}
