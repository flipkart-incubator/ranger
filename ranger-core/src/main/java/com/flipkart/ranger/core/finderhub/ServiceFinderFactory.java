package com.flipkart.ranger.core.finderhub;

import com.flipkart.ranger.core.finder.ServiceFinder;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceRegistry;

/**
 *
 */
public interface ServiceFinderFactory<T, R extends ServiceRegistry<T>, U extends Criteria<T>> {

    ServiceFinder<T, R, U> buildFinder(final Service service);

}
