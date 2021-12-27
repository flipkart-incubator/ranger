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
import com.flipkart.ranger.client.utils.CriteriaUtils;
import com.flipkart.ranger.core.finderhub.ServiceDataSource;
import com.flipkart.ranger.core.finderhub.ServiceFinderFactory;
import com.flipkart.ranger.core.finderhub.ServiceFinderHub;
import com.flipkart.ranger.core.model.Deserializer;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.ServiceRegistry;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@Getter
public abstract class AbstractRangerHubClient<T, R extends ServiceRegistry<T>, D extends Deserializer<T>> implements RangerHubClient<T> {

    private final String namespace;
    private final ObjectMapper mapper;
    private final D deserializer;

    private int nodeRefreshTimeMs;
    private ServiceFinderHub<T, R> hub;
    private final Predicate<T> initialCriteria;
    private final boolean alwaysUseInitialCriteria;

    protected AbstractRangerHubClient(
            String namespace,
            ObjectMapper mapper,
            int nodeRefreshTimeMs,
            Predicate<T> initialCriteria,
            D deserializer,
            boolean alwaysUseInitialCriteria
    ){
        this.namespace = namespace;
        this.mapper = mapper;
        this.nodeRefreshTimeMs = nodeRefreshTimeMs;
        this.initialCriteria = initialCriteria;
        this.deserializer = deserializer;
        this.alwaysUseInitialCriteria = alwaysUseInitialCriteria;
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
        return getNode(service, initialCriteria);
    }

    public List<ServiceNode<T>> getAllNodes(
            final Service service
    ){
        return getAllNodes(service, initialCriteria);
    }

    public Optional<ServiceNode<T>> getNode(
            final Service service,
            final Predicate<T> criteria
    ){
        return  this.getHub().finder(service).flatMap(trServiceFinder -> trServiceFinder.get(
                CriteriaUtils.getCriteria(alwaysUseInitialCriteria, initialCriteria, criteria))
        );
    }

    public List<ServiceNode<T>> getAllNodes(
            final Service service,
            final Predicate<T> criteria
    ){
        return this.getHub().finder(service).map(trServiceFinder -> trServiceFinder.getAll(
                CriteriaUtils.getCriteria(alwaysUseInitialCriteria, initialCriteria, criteria))
        ).orElse(Collections.emptyList());
    }

    public Collection<Service> getRegisteredServices() {
        try{
            return this.getHub().getServiceDataSource().services();
        }catch (Exception e){
            log.warn("Call to a hub failed with exception, {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    protected abstract ServiceFinderHub<T, R> buildHub();

    protected abstract ServiceDataSource buildServiceDataSource();

    protected abstract ServiceFinderFactory<T, R> buildFinderFactory();

}

