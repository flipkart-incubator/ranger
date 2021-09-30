package com.flipkart.ranger.http.serviceprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.model.NodeDataSink;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.serviceprovider.BaseServiceProviderBuilder;
import com.flipkart.ranger.core.serviceprovider.ServiceProvider;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.serde.HttpRequestDataSerializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpShardedServiceProviderBuilder<T> extends BaseServiceProviderBuilder<T, HttpShardedServiceProviderBuilder<T>, HttpRequestDataSerializer<T>> {

    private HttpClientConfig clientConfig;
    private ObjectMapper mapper;

    public HttpShardedServiceProviderBuilder<T> withClientConfiguration(final HttpClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        return this;
    }

    public HttpShardedServiceProviderBuilder<T> withObjectMapper(final ObjectMapper mapper){
        this.mapper = mapper;
        return this;
    }

    @Override
    public ServiceProvider<T, HttpRequestDataSerializer<T>> build() {
        return super.buildProvider();
    }

    @Override
    protected NodeDataSink<T, HttpRequestDataSerializer<T>> dataSink(Service service) {
        return new HttpNodeDataSink<>(service, clientConfig, mapper);
    }
}
