package com.flipkart.ranger.finder;

public interface ServiceVisitor<T> {
    T visit(CuratorService curatorService);

    T visit(HttpService httpService);
}
