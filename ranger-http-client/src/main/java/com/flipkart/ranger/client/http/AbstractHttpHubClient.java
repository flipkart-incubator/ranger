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
package com.flipkart.ranger.client.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.client.Constants;
import com.flipkart.ranger.client.RangerHubClient;
import com.flipkart.ranger.core.finderhub.ServiceDataSource;
import com.flipkart.ranger.core.finderhub.ServiceFinderFactory;
import com.flipkart.ranger.core.finderhub.ServiceFinderHub;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.ServiceRegistry;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Optional;

@Slf4j
@Getter
public abstract class AbstractHttpHubClient<T, C extends Criteria<T>, R extends ServiceRegistry<T>> implements RangerHubClient<T, C> {

    private ServiceFinderHub<T, C, R> hub;
    private String namespace;
    private String environment;
    private ObjectMapper mapper;
    private int refreshTimeMs;
    private C criteria;

    public AbstractHttpHubClient(
            String namespace,
            String environment,
            ObjectMapper mapper,
            int refreshTimeMs,
            C criteria
    ){
        this.namespace = namespace;
        this.environment =environment;
        this.mapper = mapper;
        this.refreshTimeMs = refreshTimeMs;
        this.criteria = criteria;
    }

    public void start(){
        Preconditions.checkNotNull(mapper, "Mapper can't be null");
        Preconditions.checkNotNull(namespace, "namespace can't be null");
        Preconditions.checkNotNull(environment, "Environment can't be null");

        if (this.refreshTimeMs < Constants.MINIMUM_REFRESH_TIME) {
            log.warn("Node info update interval too low: {} ms. Has been upgraded to {} ms ",
                    this.refreshTimeMs,
                    Constants.MINIMUM_REFRESH_TIME);
        }
        this.refreshTimeMs = Math.max(Constants.MINIMUM_REFRESH_TIME, this.refreshTimeMs);
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

    public abstract ServiceFinderHub<T, C, R> buildHub();

    public abstract ServiceDataSource getServiceDataSource();

    public abstract ServiceFinderFactory<T,C, R> getFinderFactory();

}

