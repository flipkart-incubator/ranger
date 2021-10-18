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
package com.flipkart.ranger.server.manager;

import com.flipkart.ranger.server.bundle.RangerServerBundle;
import io.dropwizard.lifecycle.Managed;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Getter
@Slf4j
public class RangerBundleManager implements Managed {

    private final CuratorFramework curatorFramework;
    private final RangerServerBundle rangerServerBundle;

    @Inject
    public RangerBundleManager(
            CuratorFramework curatorFramework,
            RangerServerBundle rangerServerBundle
    ){
        this.curatorFramework = curatorFramework;
        this.rangerServerBundle = rangerServerBundle;
    }


    @Override
    public void start() {
        log.info("Starting the ranger client manager");

        curatorFramework.start();
        try {
            curatorFramework.blockUntilConnected();
        }
        catch (InterruptedException e) {
            log.error("Curator block interrupted", e);
        }
        rangerServerBundle.start();
        log.info("Started the ranger client manager");
    }

    @Override
    public void stop() {
        log.info("Stopping the ranger client manager");
        rangerServerBundle.stop();
        curatorFramework.close();
        log.info("Stopped the ranger client manager");
    }
}
