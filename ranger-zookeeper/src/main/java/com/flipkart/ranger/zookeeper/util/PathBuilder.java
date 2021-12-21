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

package com.flipkart.ranger.zookeeper.util;

import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PathBuilder {

    public static String registeredServicesPath(final String namespace){
        return String.format("/%s", namespace);
    }

    public static String servicePath(final Service service) {
        return String.format("/%s/%s", service.getNamespace(), service.getServiceName());
    }

    public static<T> String instancePath(final Service service, final ServiceNode<T> node) {
        return String.format("/%s/%s/%s", service.getNamespace(), service.getServiceName(), node.representation());
    }
}
