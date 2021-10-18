package com.flipkart.ranger.server.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import javax.inject.Singleton;

@Singleton
@Slf4j
public class RangerHealthCheck extends HealthCheck {

    private final CuratorFramework curatorFramework;

    public RangerHealthCheck(CuratorFramework curatorFramework){
        this.curatorFramework = curatorFramework;
    }


    @Override
    protected Result check() {
        return curatorFramework.getZookeeperClient().isConnected() ?
                Result.healthy("Service is healthy") : Result.unhealthy("Can't connect to zookeeper");
    }
}
