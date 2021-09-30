package com.flipkart.ranger.http;

import com.flipkart.ranger.http.serviceprovider.HttpShardedServiceProviderBuilder;

public class HttpServiceProviderBuilders {

    private HttpServiceProviderBuilders() {
        throw new InstantiationError("Must not instantiate this class");
    }

    public static <T> HttpShardedServiceProviderBuilder<T> httpServiceProviderBuilder() {
        return new HttpShardedServiceProviderBuilder<>();
    }
}
