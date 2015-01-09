package com.flipkart.ranger;

import com.flipkart.ranger.serviceprovider.ServiceProviderBuilder;
import com.flipkart.ranger.finder.unsharded.UnshardedClusterInfo;

public class ServiceProviderBuilders {
    public static <T> ServiceProviderBuilder<T> shardedServiceProviderBuilder() {
        return new ServiceProviderBuilder<T>();
    }

    public static ServiceProviderBuilder<UnshardedClusterInfo> unshardedServiceProviderBuilder() {
        return new ServiceProviderBuilder<UnshardedClusterInfo>();
    }
}
