package com.flipkart.ranger.common.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ShardInfoTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String getResource(String path) {
        final InputStream data = ShardInfoTest.class.getClassLoader().getResourceAsStream(path);
        return new BufferedReader(
                new InputStreamReader(data))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    @SneakyThrows
    public static <T> T getResource(String path, Class<T> klass) {
        final String data = getResource(path);
        return mapper.readValue(data, klass);
    }

    @Test
    public void testShardInfo(){
        ShardInfo shardInfo1 = getResource("fixtures/env1.json", ShardInfo.class);
        ShardInfo shardInfo2 = getResource("fixtures/env2.json", ShardInfo.class);

        Assert.assertNotNull(shardInfo1);
        Assert.assertNotNull(shardInfo2);

        Assert.assertNotEquals(shardInfo1, shardInfo2);
        Arrays.asList(shardInfo1, shardInfo2).forEach(shardInfo -> Assert.assertEquals("e", shardInfo.getEnvironment()));
        Assert.assertEquals("r", shardInfo1.getRegion());
        Assert.assertNull(shardInfo2.getRegion());
    }
}
