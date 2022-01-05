/*
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
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
package io.appform.ranger.zk.server.lifecycle;

import io.appform.ranger.common.server.ShardInfo;
import io.appform.ranger.core.signals.Signal;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import javax.inject.Singleton;
import java.util.Collections;

@Slf4j
@Singleton
public class CuratorLifecycle extends Signal<ShardInfo> {

    private final CuratorFramework curatorFramework;

    public CuratorLifecycle(CuratorFramework curatorFramework) {
        super(() -> null, Collections.emptyList());
        this.curatorFramework = curatorFramework;
    }

    @Override
    public void start() {
        log.info("Starting the curator");

        curatorFramework.start();
        try {
            curatorFramework.blockUntilConnected();
        }
        catch (InterruptedException e) {
            log.error("Curator block interrupted", e);
            Thread.currentThread().interrupt();
        }
        log.info("Started the curator");
    }

    @Override
    public void stop() {
        curatorFramework.close();
    }
}
