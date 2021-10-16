package com.flipkart.ranger.http;

import com.flipkart.ranger.http.servicefinder.HttpShardedServiceFinderBuilder;
import com.flipkart.ranger.http.servicefinder.HttpUnshardedServiceFinderBuilider;

public class HttpServiceFinderBuilders {

    private void HttpServiceProviderBuilders(){
        throw new InstantiationError("Must not instantiate this class");
    }

    public static <T> HttpShardedServiceFinderBuilder<T> httpShardedServiceFinderBuilder(){
        return new HttpShardedServiceFinderBuilder<>();
    }

    public static <T> HttpUnshardedServiceFinderBuilider<T> httpUnshardedServiceFinderBuilider(){
        return new HttpUnshardedServiceFinderBuilider<>();
    }
}
