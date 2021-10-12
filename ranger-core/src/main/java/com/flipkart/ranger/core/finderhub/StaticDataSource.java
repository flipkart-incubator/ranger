package com.flipkart.ranger.core.finderhub;

import com.flipkart.ranger.core.model.Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

/*
A static data source to be used when we know the services beforehand and don't have to fetch from a source.
 */

@Slf4j
@AllArgsConstructor
public class StaticDataSource implements ServiceDataSource{

    private final List<Service> services;

    @Override
    public Collection<Service> services() throws Exception {
        return services;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
