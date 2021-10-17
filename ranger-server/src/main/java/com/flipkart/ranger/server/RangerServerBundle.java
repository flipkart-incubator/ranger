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
package com.flipkart.ranger.server;

import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.server.config.RangerConfiguration;
import com.flipkart.ranger.server.manager.RangerClientManager;
import com.flipkart.ranger.server.resources.RangerResource;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataDeserializer;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public abstract class RangerServerBundle<
        T,
        C extends Criteria<T>,
        U extends Configuration,
        D extends ZkNodeDataDeserializer<T>> implements ConfiguredBundle<U> {

    protected abstract RangerConfiguration withRangerConfiguration(U configuration);

    protected abstract D withDeserializer(U configuration);

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(U configuration, Environment environment) {
        val rangerConfiguration = withRangerConfiguration(configuration);
        val clientManager = new RangerClientManager<T, C, D>(
                rangerConfiguration,
                environment.getObjectMapper(),
                withDeserializer(configuration)
        );
        val rangerResource = new RangerResource<>(clientManager);
        environment.lifecycle().manage(clientManager);
        environment.jersey().register(rangerResource);
    }
}
