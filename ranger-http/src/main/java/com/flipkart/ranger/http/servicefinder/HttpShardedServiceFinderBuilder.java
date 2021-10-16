/**
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.ranger.http.servicefinder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.finder.SimpleShardedServiceFinder;
import com.flipkart.ranger.core.finder.SimpleShardedServiceFinderBuilder;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.NodeDataSource;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.http.config.HttpClientConfig;
import com.flipkart.ranger.http.serde.HTTPResponseDataDeserializer;

/**
 *
 */
public class HttpShardedServiceFinderBuilder<T, C extends Criteria<T>> extends SimpleShardedServiceFinderBuilder<T, HttpShardedServiceFinderBuilder<T, C>, HTTPResponseDataDeserializer<T>, C> {

    private HttpClientConfig clientConfig;
    private ObjectMapper mapper;

    public HttpShardedServiceFinderBuilder<T, C> withClientConfig(final HttpClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        return this;
    }

    public HttpShardedServiceFinderBuilder<T, C> withObjectMapper(final ObjectMapper mapper){
        this.mapper = mapper;
        return this;
    }

    @Override
    public SimpleShardedServiceFinder<T, C> build() {
        return buildFinder();
    }

    @Override
    protected NodeDataSource<T, HTTPResponseDataDeserializer<T>> dataSource(Service service) {
        return new HttpNodeDataSource<>(service, clientConfig, mapper);
    }

}
