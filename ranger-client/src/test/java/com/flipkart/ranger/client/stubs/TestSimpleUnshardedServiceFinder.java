package com.flipkart.ranger.client.stubs;

import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinder;
import com.flipkart.ranger.core.finder.SimpleUnshardedServiceFinderBuilder;
import com.flipkart.ranger.core.model.*;
import com.google.common.collect.Lists;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

@Builder
public class TestSimpleUnshardedServiceFinder <T, C extends Criteria<T>>
        extends SimpleUnshardedServiceFinderBuilder<TestShardInfo, TestSimpleUnshardedServiceFinder<T, C>, Deserializer<TestShardInfo>, Criteria<TestShardInfo>> {

    @Override
    public SimpleUnshardedServiceFinder<TestShardInfo, Criteria<TestShardInfo>> build() {
        return buildFinder();
    }

    @Override
    protected NodeDataSource<TestShardInfo, Deserializer<TestShardInfo>> dataSource(Service service) {
        return new NodeDataSource<TestShardInfo, Deserializer<TestShardInfo>>() {
            @Override
            public Optional<List<ServiceNode<TestShardInfo>>> refresh(Deserializer<TestShardInfo> deserializer) {
                return Optional.of(
                        Lists.newArrayList(
                                new ServiceNode<>("localhost", 9200, TestShardInfo.builder().shardId(1).build())
                        )
                );
            }

            @Override
            public void start() {

            }

            @Override
            public void ensureConnected() {

            }

            @Override
            public void stop() {

            }

            @Override
            public boolean isActive() {
                return true;
            }
        };
    }
}

