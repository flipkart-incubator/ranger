package com.flipkart.ranger.core.utils;

import com.flipkart.ranger.core.model.ShardedCriteria;
import com.flipkart.ranger.core.model.FilterCriteria;
import com.flipkart.ranger.core.units.TestNodeData;

public class CriteriaUtils {

    public static ShardedCriteria<TestNodeData> getShardedCriteria(int shardId){
        return () -> TestNodeData.builder().nodeId(shardId).build();
    }

    public static FilterCriteria<TestNodeData> getFilterCriteria(int shardId){
        return nodeData -> nodeData.getNodeId() == shardId;
    }
}
