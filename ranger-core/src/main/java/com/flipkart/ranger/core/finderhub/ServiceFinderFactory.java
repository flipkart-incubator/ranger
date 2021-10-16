package com.flipkart.ranger.core.finderhub;

import com.flipkart.ranger.core.finder.ServiceFinder;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceRegistry;

/**
 *
 */
public interface ServiceFinderFactory<T, C extends Criteria<T>, R extends ServiceRegistry<T>> {

    ServiceFinder<T, C, R> buildFinder(final Service service);

}
