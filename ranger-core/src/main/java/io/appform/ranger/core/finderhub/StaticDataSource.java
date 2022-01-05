/*
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.appform.ranger.core.finderhub;

import io.appform.ranger.core.model.Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Set;

/*
A static data source to be used when we know the services beforehand and don't have to fetch from a source.
 */

@Slf4j
@AllArgsConstructor
public class StaticDataSource implements ServiceDataSource{

    private final Set<Service> services;

    @Override
    public Collection<Service> services() {
        return services;
    }

    @Override
    public void start() {
        //Nothing to do here
    }

    @Override
    public void stop() {
        //Nothing to do here
    }
}
