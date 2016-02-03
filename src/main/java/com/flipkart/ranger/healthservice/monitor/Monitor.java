package com.flipkart.ranger.healthservice.monitor;

/**
 * An generic interface to monitor any entity
 *
 * @param <T>
 */
public interface Monitor<T> {
    /**
     * trigger a single check of the monitor service
     */
    T monitor();

    /**
     * enable the monitor
     */
    void disable();

    /**
     * disable the monitor
     */
    void enable();
}
