package com.flipkart.ranger.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ResourceHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getResource(String path) {
        final InputStream data = ResourceHelper.class.getClassLoader().getResourceAsStream(path);
        return new BufferedReader(
                new InputStreamReader(data))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    @SneakyThrows
    public static <T> T getResource(String path, Class<T> klass) {
        final String data = getResource(path);
        return objectMapper.readValue(data, klass);
    }

}
