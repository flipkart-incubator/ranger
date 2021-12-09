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
package com.flipkart.ranger.server;

import com.flipkart.ranger.client.RangerHubClient;
import com.flipkart.ranger.common.server.ShardInfo;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.server.bundle.RangerServerBundle;
import com.flipkart.ranger.server.healthcheck.RangerHealthCheck;
import com.flipkart.ranger.server.manager.RangerBundleManager;
import com.flipkart.ranger.server.util.RangerServerUtils;
import com.flipkart.ranger.zookeeper.serde.ZkNodeDataDeserializer;
import com.google.common.collect.Lists;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;

@Slf4j
public class App extends Application<AppConfiguration> {

    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {

    }

    /**
     * The reason why rangerServerBundle.run is used here instead of initializing in the initialize method and letting run automatically called.
     *
     * a) RangerServerBundle's closure contains a rangerHubClient within it, now this client could either be a ZK client or the httpClient.
     * b) A ZK client would need the curatorFramework and the http client doesn't need anything.
     * c) So the RangerServerBundle couldn't contain a curatorFramework and that has to be defined outside, for that we need the AppConfiguration, which
     *    is available in the run as an arg to be used,
     * d) Also don't want to create two bundle managers (one for http and one for ZK)
     *
     * @param appConfiguration  {@link AppConfiguration} The appConfiguration provided
     * @param environment       {@link Environment} The environment in scope.
     */
    @Override
    public void run(AppConfiguration appConfiguration, Environment environment) {
        val rangerConfiguration = appConfiguration.getRangerConfiguration();
        RangerServerUtils.verifyPreconditions(rangerConfiguration);
        val curatorFramework = RangerServerUtils.buildCurator(rangerConfiguration);

        val rangerServerBundle = new RangerServerBundle<ShardInfo, Criteria<ShardInfo>, ZkNodeDataDeserializer<ShardInfo>,
                AppConfiguration>() {

            @Override
            protected List<RangerHubClient<ShardInfo, Criteria<ShardInfo>>> withHubs(AppConfiguration configuration) {
                return Lists.newArrayList(
                        RangerServerUtils.buildRangerHub(curatorFramework, rangerConfiguration, environment.getObjectMapper())
                );
            }

            @Override
            protected boolean withInitialRotationStatus(AppConfiguration configuration) {
                return appConfiguration.isInitialRotationStatus();
            }
        };
        rangerServerBundle.run(appConfiguration, environment);

        val rangerClientManager = new RangerBundleManager(curatorFramework, rangerServerBundle);
        environment.lifecycle().manage(rangerClientManager);

        environment.healthChecks().register(
                "ranger-health-check",
                new RangerHealthCheck(rangerClientManager.getCuratorFramework())
        );
    }
}
