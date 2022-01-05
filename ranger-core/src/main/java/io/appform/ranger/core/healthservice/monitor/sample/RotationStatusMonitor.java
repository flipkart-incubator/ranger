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

package io.appform.ranger.core.healthservice.monitor.sample;

import io.appform.ranger.core.healthcheck.HealthcheckStatus;
import io.appform.ranger.core.healthservice.TimeEntity;
import io.appform.ranger.core.healthservice.monitor.IsolatedHealthMonitor;
import lombok.val;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * This is a simple service in/out rotation check monitor
 * Initialize this class with a filePath (location of the file)
 * This monitor then:
 * <ul>
 * <li>
 * Marks service as unhealthy when the file does not exist
 * </li>
 * <li>
 * Marks service as healthy when the file exists
 * </li>
 * </ul>
 */
public class RotationStatusMonitor extends IsolatedHealthMonitor<HealthcheckStatus> {

    private String filePath;

    /**
     * @param filePath location of the file
     */
    public RotationStatusMonitor(String filePath) {
        this(new TimeEntity(1, TimeUnit.SECONDS), filePath);
    }

    /**
     * @param timeEntity how often the {@link #monitor()} check needs to be executed
     * @param filePath   location of the file
     */
    public RotationStatusMonitor(TimeEntity timeEntity, String filePath) {
        super(RotationStatusMonitor.class.getSimpleName(), timeEntity);
        this.filePath = filePath;
    }

    /**
     * @param timeEntity               how often the {@link #monitor()} check needs to be executed
     * @param stalenessAllowedInMillis after how long (in milliseconds) should the monitor be regarded as stale (default: 60 seconds)
     * @param filePath                 location of the file
     */
    public RotationStatusMonitor(TimeEntity timeEntity, long stalenessAllowedInMillis, String filePath) {
        super(RotationStatusMonitor.class.getSimpleName(), timeEntity, stalenessAllowedInMillis);
        this.filePath = filePath;
    }

    /**
     * checks if the rotation file exists or not
     *
     * @return {@link HealthcheckStatus#healthy} if file exists else {@link HealthcheckStatus#unhealthy} when file doesnt exist
     */
    @Override
    public HealthcheckStatus monitor() {
        val file = new File(filePath);
        return file.exists() ? HealthcheckStatus.healthy : HealthcheckStatus.unhealthy;
    }
}
