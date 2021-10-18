package com.flipkart.ranger.client.stubs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TestShardInfo {
    private int shardId;
}
