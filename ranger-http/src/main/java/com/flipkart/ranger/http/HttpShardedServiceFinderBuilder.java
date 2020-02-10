package com.flipkart.ranger.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.finder.sharded.SimpleShardedServiceFinder;
import com.flipkart.ranger.core.finder.sharded.SimpleShardedServiceFinderBuilder;
import com.flipkart.ranger.core.model.NodeDataSource;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.http.config.HttpClientConfig;

/**
 *
 */
public class HttpShardedServiceFinderBuilder<T> extends SimpleShardedServiceFinderBuilder<T, HttpShardedServiceFinderBuilder<T>> {

    private HttpClientConfig clientConfig;

    public HttpShardedServiceFinderBuilder<T> withClientConfig(final HttpClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        return this;
    }

    @Override
    public SimpleShardedServiceFinder<T> build() {
        return buildFinder();
    }

    @Override
    protected NodeDataSource<T> dataSource(
            Service service) {
        return new HttpNodeDataSource<>(service, clientConfig, new ObjectMapper());
    }
}
