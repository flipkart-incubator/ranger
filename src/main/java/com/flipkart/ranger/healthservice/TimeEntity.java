/**
 * Copyright 2016 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.ranger.healthservice;

import com.google.common.base.MoreObjects;

import java.util.concurrent.TimeUnit;

/**
 * A simple time unit wrapper for any task, with initialDelay, interval and unit
 */
public class TimeEntity {

    private long initialDelay;
    private long timeInterval;
    private TimeUnit timeUnit;

    /**
     * defaults initial delay to 0 timeunits
     *
     * @param timeInterval repeat time interval of task
     * @param timeUnit     unit of time, for tracking the interval
     */
    public TimeEntity(long timeInterval, TimeUnit timeUnit) {
        this(0, timeInterval, timeUnit);
    }
    
    /**
     * @param initialDelay initial delay for triggering the task
     * @param timeInterval repeat time interval of task
     * @param timeUnit     unit of time, for tracking the interval
     */
    public TimeEntity(long initialDelay, long timeInterval, TimeUnit timeUnit) {
        this.initialDelay = initialDelay;
        this.timeInterval = timeInterval;
        this.timeUnit = timeUnit;
    }
    
    /**
     * @return a TimeEntity with time interval of every second
     */
    public static TimeEntity everySecond() {
        return new TimeEntity(0, 1, TimeUnit.SECONDS);
    }

    /**
     * @return a TimeEntity with time interval of every minute
     */
    public static TimeEntity everyMinute() {
        return new TimeEntity(0, 1, TimeUnit.MINUTES);
    }

    /**
     * @return a TimeEntity with time interval of every hour
     */
    public static TimeEntity everyHour() {
        return new TimeEntity(0, 1, TimeUnit.HOURS);
    }

    /**
     * @return a TimeEntity with time interval of every day
     */
    public static TimeEntity everyDay() {
        return new TimeEntity(0, 1, TimeUnit.DAYS);
    }

    public long getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(long timeInterval) {
        this.timeInterval = timeInterval;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("initialDelay", initialDelay)
                .add("timeInterval", timeInterval)
                .add("timeUnit", timeUnit)
                .toString();
    }
}
