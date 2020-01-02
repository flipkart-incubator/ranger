package com.flipkart.ranger.core.finderhub;

import com.flipkart.ranger.core.model.Service;

import java.util.Collection;

/**
 *
 */
public interface ServiceDataSource {

    Collection<Service> services() throws Exception;

    void start();
    void stop();
}
