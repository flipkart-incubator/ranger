package com.flipkart.ranger.http.servicefinder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.finder.sharded.SimpleShardedServiceFinder;
import com.flipkart.ranger.core.finder.sharded.SimpleShardedServiceFinderBuilder;
import com.flipkart.ranger.core.model.NodeDataSource;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.serde.HTTPResponseDataDeserializer;

/**
 *
 */
public class HttpShardedServiceFinderBuilder<T, D extends HTTPResponseDataDeserializer<T>> extends SimpleShardedServiceFinderBuilder<T, HttpShardedServiceFinderBuilder<T, D>, D> {

    private HttpClientConfig clientConfig;

    public HttpShardedServiceFinderBuilder<T, D> withClientConfig(final HttpClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        return this;
    }

    @Override
    public SimpleShardedServiceFinder<T> build() {
        return buildFinder();
    }

    @Override
    protected NodeDataSource<T, D> dataSource(Service service) {
        return new HttpNodeDataSource<>(service, clientConfig, new ObjectMapper());
    }
}
