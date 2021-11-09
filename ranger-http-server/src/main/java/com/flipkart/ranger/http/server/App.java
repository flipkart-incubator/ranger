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
package com.flipkart.ranger.http.server;

import com.codahale.metrics.health.HealthCheck;
import com.flipkart.ranger.client.RangerHubClient;
import com.flipkart.ranger.common.server.ShardInfo;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.http.serde.HTTPResponseDataDeserializer;
import com.flipkart.ranger.http.server.manager.RangerHttpBundleManager;
import com.flipkart.ranger.http.server.util.RangerHttpServerUtils;
import com.flipkart.ranger.server.bundle.RangerServerBundle;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.val;

import java.util.List;
import java.util.stream.Collectors;

public class App extends Application<AppConfiguration> {

    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {

    }

    @Override
    public void run(AppConfiguration appConfiguration, Environment environment) {
        val rangerConfiguration = appConfiguration.getRangerConfiguration();
        RangerHttpServerUtils.verifyPreconditions(rangerConfiguration);

        val rangerServerBundle = new RangerServerBundle<ShardInfo, Criteria<ShardInfo>, HTTPResponseDataDeserializer<ShardInfo>,
                AppConfiguration>() {

            @Override
            protected List<RangerHubClient<ShardInfo, Criteria<ShardInfo>>> withHubs(AppConfiguration configuration) {
                val clientConfigs = configuration.getRangerConfiguration().getHttpClientConfigs();
                return clientConfigs.stream().map(clientConfig ->
                        RangerHttpServerUtils.buildRangerHub(
                                rangerConfiguration.getNamespace(),
                                rangerConfiguration.getNodeRefreshTimeMs(),
                                clientConfig,
                                environment.getObjectMapper()
                )).collect(Collectors.toList());
            }

            @Override
            protected boolean withInitialRotationStatus(AppConfiguration configuration) {
                return appConfiguration.isInitialRotationStatus();
            }
        };
        rangerServerBundle.run(appConfiguration, environment);

        val rangerClientManager = new RangerHttpBundleManager(rangerServerBundle);
        environment.lifecycle().manage(rangerClientManager);
        environment.healthChecks().register(
                "ranger-http-health-check",
                new HealthCheck() {
                    @Override
                    protected Result check() {
                        return Result.healthy();
                    }
                });
    }
}
