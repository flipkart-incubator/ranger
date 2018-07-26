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

package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.PathBuilder;
import jdk.internal.joptsimple.internal.Strings;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CuratorSourceConfig extends SourceConfig{
    private static final Logger logger = LoggerFactory.getLogger(CuratorSourceConfig.class);

    private CuratorFramework curatorFramework;

    private String connectionString;
    private String namespace;
    private String serviceName;

    public CuratorSourceConfig(CuratorFramework curatorFramework) {
        super(ServiceType.CURATOR);
        this.curatorFramework = curatorFramework;
    }

    public CuratorFramework getCuratorFramework() {
        return curatorFramework;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public boolean isRunning() {
        return curatorFramework != null
                && (curatorFramework.getState() == CuratorFrameworkState.STARTED);
    }

    public <T> T accept(ServiceVisitor<T> serviceVisitor) {
        return serviceVisitor.visit(this);
    }

//    public void start() throws Exception{
//        curatorFramework.blockUntilConnected();
//        logger.debug("Connected to zookeeper cluster");
//        curatorFramework.newNamespaceAwareEnsurePath(PathBuilder.path(this))
//                .ensure(curatorFramework.getZookeeperClient());
//    }

//    public void stop() throws Exception{
//        curatorFramework.close();
//        logger.debug("Stopping Curator Service");
//    }

}
