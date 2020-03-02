package com.flipkart.ranger.core.model;

import java.util.List;
import java.util.Optional;

/**
 *
 */
public interface NodeDataSource<T, D extends Deserializer<T>> extends NodeDataStoreConnector<T> {
    Optional<List<ServiceNode<T>>> refresh(D deserializer);

    default long healthcheckZombieCheckThresholdTime(Service service) {
        return System.currentTimeMillis() - 60000; //1 Minute
    }
}
