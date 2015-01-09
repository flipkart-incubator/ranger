package com.flipkart.ranger;

import com.flipkart.ranger.serviceprovider.ServiceProviderBuilder;
import com.flipkart.ranger.finder.unsharded.UnshardedClusterInfo;

public class ServiceDiscoveryBuilders {
    public static <T> ServiceProviderBuilder<T> shardedServiceDiscoveryBuilder() {
        return new ServiceProviderBuilder<T>();
    }

    public static ServiceProviderBuilder<UnshardedClusterInfo> unshardedServiceDiscoveryBuilder() {
        return new ServiceProviderBuilder<UnshardedClusterInfo>();
    }
}
