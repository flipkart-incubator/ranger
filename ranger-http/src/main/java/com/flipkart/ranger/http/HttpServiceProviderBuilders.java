package com.flipkart.ranger.http;

import com.flipkart.ranger.http.serviceprovider.HttpServiceProviderBuilder;

public class HttpServiceProviderBuilders {

    private HttpServiceProviderBuilders() {
        throw new InstantiationError("Must not instantiate this class");
    }

    public static <T> HttpServiceProviderBuilder<T> httpServiceProviderBuilder() {
        return new HttpServiceProviderBuilder<>();
    }
}
