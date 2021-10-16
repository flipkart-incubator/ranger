package com.flipkart.ranger.core.utils;

import com.flipkart.ranger.core.units.TestNodeData;

public class CriteriaUtils {
    
    public static TestNodeData getCriteria(int shardId){
        return new TestNodeData(shardId);
    }
}
