package com.flipkart.ranger.finder;

public interface SourceConfigVisitor<T> {
    T visit(HttpSourceConfig httpSourceConfig) throws Exception;
    T visit(ZookeeperSourceConfig zookeeperSourceConfig) throws Exception;
    T visit(CuratorFrameworkConfig curatorFrameworkConfig) throws Exception;
}