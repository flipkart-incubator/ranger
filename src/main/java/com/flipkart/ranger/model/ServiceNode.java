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

package com.flipkart.ranger.model;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;

public class ServiceNode<T> {
    private String host;
    private int port;
    private T nodeData;
    private HealthcheckStatus healthcheckStatus = HealthcheckStatus.healthy;
    private long lastUpdatedTimeStamp = Long.MIN_VALUE;

    public ServiceNode() {
    }

    public ServiceNode(String host, int port, T nodeData) {
        this.host = host;
        this.port = port;
        this.nodeData = nodeData;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public T getNodeData() {
        return nodeData;
    }

    public void setNodeData(T nodeData) {
        this.nodeData = nodeData;
    }

    public String representation() {
        return String.format("%s:%d", host, port);
    }

    public HealthcheckStatus getHealthcheckStatus() {
        return healthcheckStatus;
    }

    public void setHealthcheckStatus(HealthcheckStatus healthcheckStatus) {
        this.healthcheckStatus = healthcheckStatus;
    }

    public long getLastUpdatedTimeStamp() {
        return lastUpdatedTimeStamp;
    }

    public void setLastUpdatedTimeStamp(long lastUpdatedTimeStamp) {
        this.lastUpdatedTimeStamp = lastUpdatedTimeStamp;
    }
}
