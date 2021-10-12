package com.flipkart.ranger.http.servicefinderhub;

import com.flipkart.ranger.core.finderhub.ServiceFinderHub;
import com.flipkart.ranger.core.finderhub.ServiceFinderHubBuilder;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServiceFinderHubBuilder<T, R extends ServiceRegistry<T>, U extends Criteria<T, R>> extends ServiceFinderHubBuilder<T, R, U> {

    @Override
    protected void preBuild() {
        log.info("No pre-ebuild actions necessary");
    }

    @Override
    protected void postBuild(ServiceFinderHub<T, R, U> serviceFinderHub) {
        log.info("No post build actions necessary");
    }
}
