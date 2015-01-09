package com.flipkart.ranger.finder;

import org.apache.curator.framework.CuratorFramework;

public class Service {
    private CuratorFramework curatorFramework;
    private String namespace;
    private String serviceName;

    public Service(CuratorFramework curatorFramework, String namespace, String serviceName) {
        this.curatorFramework = curatorFramework;
        this.namespace = namespace;
        this.serviceName = serviceName;
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
}
