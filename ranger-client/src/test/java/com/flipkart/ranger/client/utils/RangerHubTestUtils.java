package com.flipkart.ranger.client.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.client.stubs.RangerTestHub;
import com.flipkart.ranger.client.stubs.TestCriteria;
import com.flipkart.ranger.client.stubs.TestDeserializer;
import com.flipkart.ranger.core.model.Service;

public class RangerHubTestUtils {

    public static final Service service = new Service("test-ns", "test-s");
    private static final ObjectMapper mapper = new ObjectMapper();

    private RangerHubTestUtils(){}

    public static RangerTestHub getTestHub(){
        return new RangerTestHub(service.getNamespace(), mapper, 1000, new TestCriteria(), new TestDeserializer<>());
    }
}
