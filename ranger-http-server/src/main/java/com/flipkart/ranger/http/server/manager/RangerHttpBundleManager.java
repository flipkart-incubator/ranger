package com.flipkart.ranger.http.server.manager;

import com.flipkart.ranger.server.bundle.RangerServerBundle;
import io.dropwizard.lifecycle.Managed;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@Getter
public class RangerHttpBundleManager implements Managed {

    private final RangerServerBundle rangerServerBundle;

    @Inject
    public RangerHttpBundleManager(RangerServerBundle rangerServerBundle){
        this.rangerServerBundle = rangerServerBundle;
    }

    @Override
    public void start() {
        log.info("Starting the ranger http bundle manager");
        rangerServerBundle.start();
        log.info("Started the ranger http bundle manager");
    }

    @Override
    public void stop() {
        log.info("Stopping the ranger http bundle manager");
        rangerServerBundle.stop();
        log.info("Stopped the ranger http bundle manager");
    }
}
