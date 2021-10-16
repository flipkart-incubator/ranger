package com.flipkart.ranger.http.servicefinder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinder;
import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinderBuilder;
import com.flipkart.ranger.core.model.*;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.serde.HTTPResponseDataDeserializer;

public class HttpUnshardedServiceFinderBuilider<T, C extends Criteria<T>>
        extends SimpleUnshardedServiceFinderBuilder<T, HttpUnshardedServiceFinderBuilider<T, C>, HTTPResponseDataDeserializer<T>, C> {

    private HttpClientConfig clientConfig;
    private ObjectMapper mapper;

    public HttpUnshardedServiceFinderBuilider<T, C> withClientConfig(final HttpClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        return this;
    }

    public HttpUnshardedServiceFinderBuilider<T, C> withObjectMapper(final ObjectMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    @Override
    public SimpleUnshardedServiceFinder<T, C> build() {
        return buildFinder();
    }

    @Override
    protected NodeDataSource<T, HTTPResponseDataDeserializer<T>> dataSource(Service service) {
        return new HttpNodeDataSource<>(service, clientConfig, mapper);
    }

}

