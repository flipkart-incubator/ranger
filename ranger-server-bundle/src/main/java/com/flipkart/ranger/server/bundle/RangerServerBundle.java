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
package com.flipkart.ranger.server.bundle;

import com.flipkart.ranger.client.RangerHubClient;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Deserializer;
import com.flipkart.ranger.server.bundle.resources.RangerResource;
import com.flipkart.ranger.server.bundle.rotation.BirTask;
import com.flipkart.ranger.server.bundle.rotation.OorTask;
import com.flipkart.ranger.server.bundle.rotation.RotationStatus;
import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class RangerServerBundle<
        T,
        C extends Criteria<T>,
        D extends Deserializer<T>,
        U extends Configuration
        > implements ConfiguredBundle<U>{

    @Getter
    private List<RangerHubClient<T, C>> hubs;
    @Getter
    @VisibleForTesting
    private RotationStatus rotationStatus;

    protected abstract List<RangerHubClient<T, C>> withHubs(U configuration);

    protected abstract boolean withInitialRotationStatus(U configuration);

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(U configuration, Environment environment) {
        hubs = withHubs(configuration);
        rotationStatus = new RotationStatus(withInitialRotationStatus(configuration));

        environment.admin()
                .addTask(new OorTask(rotationStatus));
        environment.admin()
                .addTask(new BirTask(rotationStatus));

        environment.jersey().register(new RangerResource<>(hubs));
    }

    public void start(){
        log.info("Starting the ranger hubs");
        hubs.forEach(RangerHubClient::start);
    }

    public void stop(){
        log.info("Stopping the ranger hub");
        hubs.forEach(RangerHubClient::stop);
    }
}
