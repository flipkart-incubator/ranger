/**
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
package com.flipkart.ranger.zookeeper.servicefinderhub;

import com.flipkart.ranger.core.finderhub.ServiceDataSource;
import com.flipkart.ranger.core.model.Service;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
public class ZkServiceDataSource implements ServiceDataSource {

    private final String namespace;
    private String connectionString;
    private CuratorFramework curatorFramework;
    private boolean curatorProvided;

    public ZkServiceDataSource(String namespace,
                               String connectionString,
                               CuratorFramework curatorFramework){
        this.namespace = namespace;
        this.connectionString = connectionString;
        this.curatorFramework = curatorFramework;
    }

    @Override
    public Collection<Service> services() throws Exception {
        final List<String> children = curatorFramework.getChildren()
                .forPath("/");
        return children.stream()
                .map(child -> new Service(namespace, child))
                .collect(Collectors.toSet());
    }

    @Override
    public void start() {
        if(null == curatorFramework){
            Preconditions.checkNotNull(connectionString);
            log.info("Building custom curator framework");
            curatorFramework = CuratorFrameworkFactory.builder()
                    .namespace(namespace)
                    .connectString(connectionString)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 100))
                    .build();
            curatorFramework.start();
            curatorProvided = false;
        }
        try {
            curatorFramework.blockUntilConnected();
        }
        catch (InterruptedException e) {
            log.error("Curator block interrupted", e);
        }
        log.info("Service data source started. Curator state is: {}", curatorFramework.getState().name());
    }

    @Override
    public void stop() {
        log.info("Service data stopped");
        if(!curatorProvided) curatorFramework.close();
        log.info("Service data source stopped");
    }
}
