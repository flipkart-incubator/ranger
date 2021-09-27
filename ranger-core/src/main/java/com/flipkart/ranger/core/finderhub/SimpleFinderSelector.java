package com.flipkart.ranger.core.finderhub;

import com.flipkart.ranger.core.finder.ServiceFinder;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceRegistry;
import lombok.Data;

import java.util.Map;
import java.util.Optional;

@Data
public class SimpleFinderSelector<T, R extends ServiceRegistry<T>> implements ServiceFinderSelector<T, R> {

    @Override
    public Optional<ServiceFinder<T, R>> finder(Service service, Map<Service, ServiceFinder<T, R>> finders) {
        return Optional.ofNullable(finders.get(service));
    }

}
