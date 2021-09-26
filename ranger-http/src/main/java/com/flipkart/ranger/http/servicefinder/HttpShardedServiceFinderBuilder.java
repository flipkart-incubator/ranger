package com.flipkart.ranger.http.servicefinder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.finder.SimpleShardedServiceFinder;
import com.flipkart.ranger.core.finder.SimpleShardedServiceFinderBuilder;
import com.flipkart.ranger.core.model.NodeDataSource;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.serde.HTTPResponseDataDeserializer;

/**
 *
 */
public class HttpShardedServiceFinderBuilder<T> extends SimpleShardedServiceFinderBuilder<T, HttpShardedServiceFinderBuilder<T>, HTTPResponseDataDeserializer<T>> {

    private HttpClientConfig clientConfig;
    private ObjectMapper mapper;

    public HttpShardedServiceFinderBuilder<T> withClientConfig(final HttpClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        return this;
    }

    public HttpShardedServiceFinderBuilder<T> withObjectMapper(final ObjectMapper mapper){
        this.mapper = mapper;
        return this;
    }

    @Override
    public SimpleShardedServiceFinder<T> build() {
        return buildFinder();
    }

    @Override
    protected NodeDataSource<T, HTTPResponseDataDeserializer<T>> dataSource(Service service) {
        return new HttpNodeDataSource<>(service, clientConfig, mapper);
    }


}
