package com.flipkart.ranger.healthservice.monitor;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;

import java.io.File;

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
     * @return true if the monitor is disabled, else false
     */
    boolean isDisabled();




    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////  static impls  ////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * A monitor to monitor the existance of a file
     */
    class FileExistanceCheckMonitor implements Monitor<HealthcheckStatus> {
        private String filePath;

        public FileExistanceCheckMonitor(String filePath) {
            this.filePath = filePath;
        }

        /**
         * @return healthy if file exists, else returns unhealthy
         */
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
    }
}
