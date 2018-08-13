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

import com.flipkart.ranger.model.Deserializer;

public class ZookeeperSourceConfig<T> extends SourceConfig<T>{
    private String connectionString;
    private String namespace;
    private Deserializer<T> zookeeperNodeDataDecoder;
    private String serviceName;

    public ZookeeperSourceConfig(String connectionString, String namespace, String serviceName, Deserializer<T> deserializer) {
        super(ServiceType.ZOOKEEPERSOURCE);
        this.connectionString = connectionString;
        this.namespace = namespace;
        this.zookeeperNodeDataDecoder = deserializer;
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Deserializer<T> getZookeeperNodeDataDecoder() {
        return zookeeperNodeDataDecoder;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public ServiceRegistryUpdater<T> accept(SourceConfigVisitor<T> sourceConfigVisitor) throws Exception{
        return sourceConfigVisitor.visit(this);
    }
}
