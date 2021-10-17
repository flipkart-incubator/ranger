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
        log.info("No pre-build actions necessary");
    }

    @Override
    protected void postBuild(ServiceFinderHub<T,C, R> serviceFinderHub) {
        log.info("No post build actions necessary");
    }
}
