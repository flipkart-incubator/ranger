package com.flipkart.ranger.finder;

public interface ServiceVisitor<T> {
    T visit(CuratorSourceConfig curatorService);

    T visit(HttpSourceConfig httpService);
}
