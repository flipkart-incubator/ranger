package com.flipkart.ranger.healthservice.monitor;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class may be used to keep track of health in a rolling window
 * Maintains a queue of {@link HealthcheckStatus}s of size {@code rollingWindowSize}
 */
public class RollingWindowHealthQueue {

    /* size of the rolling window */
    private Integer rollingWindowSize;

    /* maximum failures allowed in the window */
    private Integer maxFailures;

    /* current failures in the window */
    private final AtomicReference<Integer> currentFailuresCount = new AtomicReference<Integer>();

    /* queue of health statuses */
    private Queue<HealthcheckStatus> statusQueue;

    /**
     * @param rollingWindowSize size of the rolling window to be maintained
     * @param maxFailures       maximum failures allowed in the window
     */
    public RollingWindowHealthQueue(Integer rollingWindowSize, Integer maxFailures) {
        this.rollingWindowSize = rollingWindowSize;
        this.maxFailures = maxFailures;
        this.currentFailuresCount.set(0);
        statusQueue = new LinkedList<>();
        if (rollingWindowSize <= 0) {
            throw new UnsupportedOperationException("rollingWindowSize cant be <= 0");
        }
        if (maxFailures > rollingWindowSize) {
            throw new UnsupportedOperationException("maxFailures cant be greater than the rollingWindowSize");
        }
    }

    /**
     * dequeues the latest incoming status, enqueues the latest health status
     * updates the failure counts accordingly
     *
     * @param currentHealthStatus current health status coming from outside
     * @return <code>true</code>  if the current failures in the queue are less than the maxFailure
     * <code>false</code> if the current failures in the queue are more than or equal to the maxFailures
     */
    public boolean checkInRollingWindow(HealthcheckStatus currentHealthStatus) {
        HealthcheckStatus oldestStatus;
        if (currentHealthStatus == HealthcheckStatus.healthy) {
            if (statusQueue.size() == rollingWindowSize) {
                oldestStatus = statusQueue.remove();
                if (oldestStatus == HealthcheckStatus.unhealthy) {
                    currentFailuresCount.getAndSet(currentFailuresCount.get() - 1);
                }
            }
            statusQueue.add(currentHealthStatus);
        } else {
            if (statusQueue.size() == rollingWindowSize) {
                oldestStatus = statusQueue.remove();
                if (oldestStatus == HealthcheckStatus.healthy) {
                    currentFailuresCount.getAndSet(currentFailuresCount.get() + 1);
                }
            } else {
                currentFailuresCount.getAndSet(currentFailuresCount.get() + 1);
            }
            statusQueue.add(currentHealthStatus);
        }
        return currentFailuresCount.get() < maxFailures;
    }
}
