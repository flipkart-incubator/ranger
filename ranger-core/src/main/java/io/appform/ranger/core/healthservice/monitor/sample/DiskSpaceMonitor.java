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

import io.appform.ranger.core.healthservice.TimeEntity;
import lombok.val;

import java.io.File;

/**
 * A simple monitor to keep track of the disk space in a partition
 */
public class DiskSpaceMonitor extends CountMonitor {

    private String partition;

    /**
     * @param partition       partition to be monitored
     * @param countThreshhold maximum freespace of partition, below which, the monitor will mark itself as unhealthy
     * @param timeEntity      how often the {@link #monitor()} check needs to be executed
     */
    public DiskSpaceMonitor(String partition, Integer countThreshhold, TimeEntity timeEntity) {
        super(DiskSpaceMonitor.class.getSimpleName(), CheckSign.GREATER_THAN, countThreshhold, timeEntity);
        this.partition = partition;
    }

    /**
     * @param partition  partition to be monitored
     * @param name       name of monitor
     * @param threshhold maximum freespace of partition, below which, the monitor will mark itself as unhealthy
     * @param timeEntity how often the {@link #monitor()} check needs to be executed
     */
    public DiskSpaceMonitor(String partition, String name, Integer threshhold, TimeEntity timeEntity) {
        super(name, CheckSign.GREATER_THAN, threshhold, timeEntity);
        this.partition = partition;
    }

    @Override
    public Number getCount() {
        val file = new File(partition);
        return file.getFreeSpace();
    }
}
