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

package com.flipkart.ranger.healthservice.monitor.sample;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class RotationStatusMonitorTest {

    final String filePath = "/tmp/rangerRotationFile.html";
    File file = new File(filePath);

    @Before
    public void setUp() throws Exception {
        deleteRotationFile();
    }

    @After
    public void tearDown() throws Exception {
        deleteRotationFile();
    }

    @Test
    public void testMonitor() throws Exception {
        deleteRotationFile();
        RotationStatusMonitor rotationStatusMonitor = new RotationStatusMonitor("/tmp/rotationFile.html");
        final HealthcheckStatus monitorResult = rotationStatusMonitor.monitor();
        Assert.assertEquals(HealthcheckStatus.unhealthy, monitorResult);
    }

    @Test
    public void testMonitor2() throws Exception {
        deleteRotationFile();
        if (file.createNewFile()) {
            RotationStatusMonitor rotationStatusMonitor = new RotationStatusMonitor(filePath);
            final HealthcheckStatus monitorResult = rotationStatusMonitor.monitor();
            Assert.assertEquals(HealthcheckStatus.healthy, monitorResult);
        } else {
            System.out.println("Unable to create file = " + filePath);
            throw new Exception("Unable to create file = " + filePath);
        }
    }

    private void deleteRotationFile() throws Exception {
        if (file.exists()) {
            if (!file.delete()) {
                System.out.println("Unable to delete file = " + filePath);
                throw new Exception("Unable to delete file = " + filePath);
            }
        }
    }
}