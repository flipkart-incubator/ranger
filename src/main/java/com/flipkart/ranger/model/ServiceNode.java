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
