package com.flipkart.ranger.server;

import com.flipkart.ranger.server.healthcheck.RangerHealthCheck;
import com.flipkart.ranger.server.manager.RangerClientManager;
import com.flipkart.ranger.server.resources.RangerResource;
import com.flipkart.ranger.server.rotation.BirTask;
import com.flipkart.ranger.server.rotation.OorTask;
import com.flipkart.ranger.server.rotation.RotationStatus;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class App extends Application<AppConfiguration> {

    private RotationStatus rotationStatus;

    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {

    }

    @Override
    public void run(AppConfiguration appConfiguration, Environment environment) {
        rotationStatus = new RotationStatus(appConfiguration.isInitialRotationStatus());
        val rangerClientManager = new RangerClientManager(appConfiguration, environment.getObjectMapper());
        environment.lifecycle().manage(rangerClientManager);
        environment.jersey().register(new RangerResource(rangerClientManager));
        environment.admin()
                .addTask(new OorTask(rotationStatus));
        environment.admin()
                        .addTask(new BirTask(rotationStatus));
        environment.healthChecks().register(
                "ranger-healthcheck", new RangerHealthCheck(rangerClientManager.getCuratorFramework()));
    }
}
