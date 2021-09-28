package com.flipkart.ranger.core.model;

import lombok.Data;

@Data
public class UnshardedCriteria implements Criteria<UnshardedClusterInfo> {

    private final UnshardedClusterInfo unshardedClusterInfo;

    @Override
    public UnshardedClusterInfo getCriteria() {
        return unshardedClusterInfo;
    }
}
