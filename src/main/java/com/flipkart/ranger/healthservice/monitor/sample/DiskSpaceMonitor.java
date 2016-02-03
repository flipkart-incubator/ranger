package com.flipkart.ranger.healthservice.monitor.sample;

import com.flipkart.ranger.healthservice.TimeEntity;

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
        File file = new File(partition);
        return file.getFreeSpace();
    }
}
