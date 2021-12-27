/*
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
package com.flipkart.ranger.client;

import com.flipkart.ranger.client.utils.CriteriaUtils;
import com.flipkart.ranger.core.finder.ServiceFinder;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.core.model.ServiceRegistry;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractRangerClient<T, R extends ServiceRegistry<T>> implements RangerClient<T> {

    private final Predicate<T> initialCriteria;
    private final boolean alwaysUseInitialCriteria;

    protected AbstractRangerClient(Predicate<T> initialCriteria, boolean alwaysUseInitialCriteria){
        this.initialCriteria = initialCriteria;
        this.alwaysUseInitialCriteria = alwaysUseInitialCriteria;
    }

    public abstract ServiceFinder<T, R> getServiceFinder();

    @Override
    public Optional<ServiceNode<T>> getNode() {
        return getServiceFinder().get(initialCriteria);
    }

    @Override
    public Optional<ServiceNode<T>> getNode(Predicate<T> criteria) {
        return getServiceFinder().get(CriteriaUtils.getCriteria(alwaysUseInitialCriteria, initialCriteria, criteria));
    }

    @Override
    public List<ServiceNode<T>> getAllNodes() {
        return getServiceFinder().getAll(initialCriteria);
    }

    @Override
    public List<ServiceNode<T>> getAllNodes(Predicate<T> criteria) {
        return getServiceFinder().getAll(CriteriaUtils.getCriteria(alwaysUseInitialCriteria, initialCriteria, criteria));
    }
}
