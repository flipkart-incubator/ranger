package com.flipkart.ranger.http;

import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.http.servicefinder.HttpShardedServiceFinderBuilder;
import com.flipkart.ranger.http.servicefinder.HttpUnshardedServiceFinderBuilider;

public class HttpServiceFinderBuilders {

    private void HttpServiceProviderBuilders(){
        throw new InstantiationError("Must not instantiate this class");
    }

    public static <T> HttpShardedServiceFinderBuilder<T, Criteria<T>> httpShardedServiceFinderBuilder(){
        return new HttpShardedServiceFinderBuilder<>();
    }

    public static <T> HttpUnshardedServiceFinderBuilider<T, Criteria<T>> httpUnshardedServiceFinderBuilider(){
        return new HttpUnshardedServiceFinderBuilider<>();
    }
}
