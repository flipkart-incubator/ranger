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
package com.flipkart.ranger.http;

import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.http.servicefinder.HttpShardedServiceFinderBuilder;
import com.flipkart.ranger.http.servicefinder.HttpUnshardedServiceFinderBuilider;

public class HttpServiceFinderBuilders {

    private void HttpServiceProviderBuilders(){
        throw new InstantiationError("Must not instantiate this class");
    }

    public static <T, C extends Criteria<T>> HttpShardedServiceFinderBuilder<T, C> httpShardedServiceFinderBuilder(){
        return new HttpShardedServiceFinderBuilder<>();
    }

    public static <T, C extends Criteria<T>> HttpUnshardedServiceFinderBuilider<T, C> httpUnshardedServiceFinderBuilider(){
        return new HttpUnshardedServiceFinderBuilider<>();
    }
}
