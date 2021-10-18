package com.flipkart.ranger.client.stubs;

import com.flipkart.ranger.core.model.Criteria;

public class TestCriteria implements Criteria<TestShardInfo> {
    public boolean apply(TestShardInfo nodeData) {
        return nodeData.getShardId() == 1;
    }
}
