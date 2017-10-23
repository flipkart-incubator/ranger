/**
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

package com.flipkart.ranger.finder;


public class ConnectionRequest {

    private final String shardName;
    private final String replicaHostName;
    private final int port;

    public ConnectionRequest(String shardName, String replicaHostName, int port) {
        this.shardName = shardName;
        this.replicaHostName = replicaHostName;
        this.port = port;
    }

    public String getShardName() {
        return shardName;
    }

    public String getReplicaHostName() {
        return replicaHostName;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionRequest that = (ConnectionRequest) o;

        if (getPort() != that.getPort()) return false;
        if (!getShardName().equals(that.getShardName())) return false;
        return getReplicaHostName().equals(that.getReplicaHostName());
    }

    @Override
    public int hashCode() {
        int result = getShardName().hashCode();
        result = 31 * result + getReplicaHostName().hashCode();
        result = 31 * result + getPort();
        return result;
    }

    @Override
    public String toString() {
        return shardName + ":" + replicaHostName + ":" + port;
    }
}
