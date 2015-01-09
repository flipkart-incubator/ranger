package com.flipkart.ranger.model;

import com.flipkart.ranger.finder.Service;

import java.util.List;

public abstract class ServiceRegistry<T> {
    private final Service service;
    private final Deserializer<T> deserializer;

    protected ServiceRegistry(Service service, Deserializer<T> deserializer) {
        this.service = service;
        this.deserializer = deserializer;
    }

    public abstract void start() throws Exception;

    public abstract void stop() throws Exception;

    abstract public void nodes(List<ServiceNode<T>> nodes);

    public Service getService() {
        return service;
    }

    public Deserializer<T> getDeserializer() {
        return deserializer;
    }
}
