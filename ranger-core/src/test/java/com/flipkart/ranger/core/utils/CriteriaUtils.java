package com.flipkart.ranger.core.utils;

import com.flipkart.ranger.core.model.ShardedCriteria;
import com.flipkart.ranger.core.model.UnshardedCriteria;
import com.flipkart.ranger.core.units.TestNodeData;

public class CriteriaUtils {

    public static ShardedCriteria<TestNodeData> getShardedCriteria(int shardId){
        return new ShardedCriteria<TestNodeData>() {
            @Override
            public TestNodeData getShard() {
                return TestNodeData.builder().nodeId(shardId).build();
            }
        };
    }

    public static UnshardedCriteria<TestNodeData> getUnshardedCriteria(int shardId){
        return new UnshardedCriteria<TestNodeData>() {
            @Override
            public boolean apply(TestNodeData nodeData) {
                return nodeData.getNodeId() == shardId;
            }
        };
    }
}
