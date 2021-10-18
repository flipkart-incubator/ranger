/*
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
package com.flipkart.ranger.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.finderhub.ServiceDataSource;
import com.flipkart.ranger.core.finderhub.ServiceFinderFactory;
import com.flipkart.ranger.core.finderhub.ServiceFinderHub;
import com.flipkart.ranger.core.model.*;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Getter
public abstract class AbstractRangerHubClient<T, C extends Criteria<T>, R extends ServiceRegistry<T>, D extends Deserializer<T>> implements RangerHubClient<T, C> {

    private final String namespace;
    private final ObjectMapper mapper;
    private final C criteria;
    private final D deserializer;

    private int nodeRefreshTimeMs;
    private ServiceFinderHub<T, C, R> hub;

    public AbstractRangerHubClient(
            String namespace,
            ObjectMapper mapper,
            int nodeRefreshTimeMs,
            C criteria,
            D deserializer
    ){
        this.namespace = namespace;
        this.mapper = mapper;
        this.nodeRefreshTimeMs = nodeRefreshTimeMs;
        this.criteria = criteria;
        this.deserializer = deserializer;
    }

    public void start(){
        Preconditions.checkNotNull(mapper, "Mapper can't be null");
        Preconditions.checkNotNull(namespace, "namespace can't be null");
        Preconditions.checkNotNull(deserializer, "deserializer can't be null");

        if (this.nodeRefreshTimeMs < RangerClientConstants.MINIMUM_REFRESH_TIME) {
            log.warn("Node info update interval too low: {} ms. Has been upgraded to {} ms ",
                    this.nodeRefreshTimeMs,
                    RangerClientConstants.MINIMUM_REFRESH_TIME);
        }
        this.nodeRefreshTimeMs = Math.max(RangerClientConstants.MINIMUM_REFRESH_TIME, this.nodeRefreshTimeMs);
        this.hub = buildHub();
        this.hub.start();
    }

    public void stop(){
        hub.stop();
    }

    public Optional<ServiceNode<T>> getNode(
            final Service service
    ){
        return getNode(service, criteria);
    }

    public Optional<List<ServiceNode<T>>> getAllNodes(
            final Service service
    ){
        return getAllNodes(service, criteria);
    }

    public Optional<ServiceNode<T>> getNode(
            final Service service,
            final C criteria
    ){
        val optionalFinder = this.getHub().finder(service);
        return optionalFinder.map(trcServiceFinder -> trcServiceFinder.get(criteria));
    }

    public Optional<List<ServiceNode<T>>> getAllNodes(
            final Service service,
            final C criteria
    ){
        val optionalFinder = this.getHub().finder(service);
        return optionalFinder.map(trcServiceFinder -> trcServiceFinder.getAll(criteria));
    }

    public Collection<Service> getServices() throws Exception {
        return this.getHub().getServiceDataSource().services();
    }

    protected abstract ServiceFinderHub<T, C, R> buildHub();

    protected abstract ServiceDataSource buildServiceDataSource();

    protected abstract ServiceFinderFactory<T,C, R> buildFinderFactory();

}

