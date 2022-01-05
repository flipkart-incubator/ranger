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
package io.appform.ranger.zookeeper.healthservice.monitor.sample;

import io.appform.ranger.core.healthcheck.HealthcheckStatus;
import io.appform.ranger.core.healthservice.monitor.sample.RotationStatusMonitor;
import lombok.val;
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
        val rotationStatusMonitor = new RotationStatusMonitor("/tmp/rotationFile.html");
        Assert.assertEquals(HealthcheckStatus.unhealthy, rotationStatusMonitor.monitor());
    }

    @Test
    public void testMonitor2() throws Exception {
        deleteRotationFile();
        if (file.createNewFile()) {
            val rotationStatusMonitor = new RotationStatusMonitor(filePath);
            val monitorResult = rotationStatusMonitor.monitor();
            Assert.assertEquals(HealthcheckStatus.healthy, rotationStatusMonitor.monitor());
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