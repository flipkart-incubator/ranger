package com.flipkart.ranger.http;

import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.http.servicefinder.HttpShardedServiceFinderBuilder;
import com.flipkart.ranger.http.servicefinder.HttpUnshardedServiceFinderBuilider;

public class HttpServiceFinderBuilders {

    private void HttpServiceProviderBuilders(){
        throw new InstantiationError("Must not instantiate this class");
    }

    public static <T, C extends Criteria<T>> HttpShardedServiceFinderBuilder<T, C> httpShardedServiceFinderBuilder(){
        return new HttpShardedServiceFinderBuilder<>();
    }

    public static <T, C extends Criteria<T>> HttpUnshardedServiceFinderBuilider<T, C> httpUnshardedServiceFinderBuilider(){
        return new HttpUnshardedServiceFinderBuilider<>();
    }
}
