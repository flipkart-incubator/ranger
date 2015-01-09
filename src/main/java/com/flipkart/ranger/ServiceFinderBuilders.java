package com.flipkart.ranger;

import com.flipkart.ranger.finder.sharded.SimpleShardedServiceFinderBuilder;
import com.flipkart.ranger.finder.unsharded.UnshardedFinderBuilder;

public class ServiceFinderBuilders {
    public static <T> SimpleShardedServiceFinderBuilder<T> shardedFinderBuilder() {
        return new SimpleShardedServiceFinderBuilder<T>();
    }

    public static UnshardedFinderBuilder unshardedFinderBuilder() {
        return new UnshardedFinderBuilder();
    }
}
