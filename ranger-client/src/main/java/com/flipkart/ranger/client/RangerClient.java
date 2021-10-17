/**
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

package com.flipkart.ranger.client;

import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Deserializer;
import com.flipkart.ranger.core.model.ServiceNode;

import java.util.List;
import java.util.Optional;

public interface RangerClient<T, C extends Criteria<T>, D extends Deserializer<T>> {

    void start();

    void stop();

    Optional<ServiceNode<T>> getNode();

    Optional<ServiceNode<T>> getNode(
            final C criteria
    );

    Optional<List<ServiceNode<T>>> getAllNodes();

    Optional<List<ServiceNode<T>>> getAllNodes(
            final C criteria
    );
}
