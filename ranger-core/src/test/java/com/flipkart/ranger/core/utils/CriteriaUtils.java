package com.flipkart.ranger.core.utils;

import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.units.TestNodeData;

public class CriteriaUtils {
    
    public static Criteria<TestNodeData> getCriteria(int shardId){
        return nodeData -> nodeData.getNodeId() == shardId;
    }
}
