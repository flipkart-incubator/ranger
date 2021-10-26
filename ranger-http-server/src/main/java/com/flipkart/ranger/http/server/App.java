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
