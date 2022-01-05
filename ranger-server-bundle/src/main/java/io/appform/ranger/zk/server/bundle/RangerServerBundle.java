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
package io.appform.ranger.zk.server.bundle;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.appform.ranger.client.RangerHubClient;
import io.appform.ranger.core.signals.Signal;
import io.appform.ranger.zk.server.bundle.resources.RangerResource;
import io.appform.ranger.zk.server.bundle.rotation.BirTask;
import io.appform.ranger.zk.server.bundle.rotation.OorTask;
import io.appform.ranger.zk.server.bundle.rotation.RotationStatus;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;

@Slf4j
public abstract class RangerServerBundle<
        T,
        U extends Configuration> implements ConfiguredBundle<U>{

    /*
        Why are we taking a list of hubs, instead of one? To be able to aggregate from different dataSources if need be
        instead of just fetching from a single dataSource.

        Please check the {@link RangerResource} for how a list is getting aggregated.

        You could also define your custom aggregation by using the {@link RangerHubClient}
     */
    @Getter
    private List<RangerHubClient<T>> hubs;
    @Getter
    private ObjectMapper mapper;

    protected void preBundle(U configuration){
        /*
            Noop here! Let the subclasses override if there are any. Not a mandatory check everywhere. Http doesn't need it, only dataStore builders will need them.
            Need not be abstract!
         */
    }

    /*
        Letting the subclasses override should they have to again. The default is set to false always. Need not be abstract, not a mandatory parameter.
     */
    @SuppressWarnings("unused")
    protected boolean withInitialRotationStatus(U configuration){
        return false;
    }

    /*
        Not a mandatory parameter (for example, needed for zk, not for http!
        Letting the subclasses override should they have to, need not be abstract. Avoids boilerplate code everywhere, the default impl!
     */
    @SuppressWarnings("unused")
    protected List<Signal<T>> withLifecycleSignals(U configuration){
        return ImmutableList.of();
    }

    protected abstract List<RangerHubClient<T>> withHubs(U configuration);

    protected abstract List<HealthCheck> withHealthChecks(U configuration);

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        /*
            Nothing to init here!
        */
    }

    @Override
    public void run(U configuration, Environment environment) {
        preBundle(configuration);

        mapper = environment.getObjectMapper();
        hubs = withHubs(configuration);

        val rotationStatus = new RotationStatus(withInitialRotationStatus(configuration));
        val lifecycleSignals = withLifecycleSignals(configuration);
        val healthChecks = withHealthChecks(configuration);

        environment.admin()
                .addTask(new OorTask(rotationStatus));
        environment.admin()
                .addTask(new BirTask(rotationStatus));

        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() {
                log.info("Starting the server manager");
                lifecycleSignals.forEach(Signal::start);
                hubs.forEach(RangerHubClient::start);
                log.info("Started the server manager");
            }

            @Override
            public void stop() {
                log.info("Stopping the server manager");
                hubs.forEach(RangerHubClient::stop);
                lifecycleSignals.forEach(Signal::stop);
                log.info("Stopped the server manager");
            }
        });
        healthChecks.forEach(healthCheck -> environment.healthChecks().register(healthCheck.getClass().getName(), healthCheck));
        environment.jersey().register(new RangerResource<>(hubs));
    }
}
