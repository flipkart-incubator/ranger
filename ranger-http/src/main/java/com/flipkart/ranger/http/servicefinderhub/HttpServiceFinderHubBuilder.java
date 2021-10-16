package com.flipkart.ranger.http.servicefinderhub;

import com.flipkart.ranger.core.finderhub.ServiceFinderHub;
import com.flipkart.ranger.core.finderhub.ServiceFinderHubBuilder;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServiceFinderHubBuilder<T, C extends Criteria<T>, R extends ServiceRegistry<T>> extends ServiceFinderHubBuilder<T, C, R> {

    @Override
    protected void preBuild() {
        log.info("No pre-ebuild actions necessary");
    }

    @Override
    protected void postBuild(ServiceFinderHub<T,C, R> serviceFinderHub) {
        log.info("No post build actions necessary");
    }
}
