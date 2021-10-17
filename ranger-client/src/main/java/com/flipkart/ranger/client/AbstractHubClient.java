package com.flipkart.ranger.client;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public abstract class AbstractHubClient<T, C extends Criteria<T>, R extends ServiceRegistry<T>> implements RangerHubClient<T, C> {

    private final String namespace;
    private final ObjectMapper mapper;
    private final C criteria;

    private int refreshTimeMs;
    private ServiceFinderHub<T, C, R> hub;

    public AbstractHubClient(
            String namespace,
            ObjectMapper mapper,
            int refreshTimeMs,
            C criteria
    ){
        this.namespace = namespace;
        this.mapper = mapper;
        this.refreshTimeMs = refreshTimeMs;
        this.criteria = criteria;
    }

    public void start(){
        Preconditions.checkNotNull(mapper, "Mapper can't be null");
        Preconditions.checkNotNull(namespace, "namespace can't be null");

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

    public abstract ServiceDataSource buildServiceDataSource();

    public abstract ServiceFinderFactory<T,C, R> getFinderFactory();

}

