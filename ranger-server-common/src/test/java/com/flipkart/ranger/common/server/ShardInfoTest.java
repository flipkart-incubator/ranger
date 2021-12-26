package com.flipkart.ranger.common.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;


public class ShardInfoTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    private String getResource(String path) {
        val data = ShardInfoTest.class.getClassLoader().getResourceAsStream(path);
        return new BufferedReader(
                new InputStreamReader(data))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    @SneakyThrows
    private  <T> T getResource(String path, Class<T> klass) {
        val data = getResource(path);
        return mapper.readValue(data, klass);
    }

    @Test
    public void testShardInfo(){
        val shardInfo1 = getResource("fixtures/env1.json", ShardInfo.class);
        val shardInfo2 = getResource("fixtures/env2.json", ShardInfo.class);
        Assert.assertNotNull(shardInfo1);
        Assert.assertNotNull(shardInfo2);
        Assert.assertNotEquals(shardInfo1, shardInfo2);
        Arrays.asList(shardInfo1, shardInfo2).forEach(shardInfo -> Assert.assertEquals("e", shardInfo.getEnvironment()));
        Assert.assertEquals("r", shardInfo1.getRegion());
        Assert.assertNull(shardInfo2.getRegion());
    }
}
