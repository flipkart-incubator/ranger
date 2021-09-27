package com.flipkart.ranger.core.finderhub;

import com.flipkart.ranger.core.finder.ServiceFinder;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceRegistry;

import java.util.Map;
import java.util.Optional;

public interface ServiceFinderSelector<T, R extends ServiceRegistry<T>> {

    Optional<ServiceFinder<T, R>> finder(Service service, Map<Service, ServiceFinder<T, R>> finders);

}
