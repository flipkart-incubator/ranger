package com.flipkart.ranger.http.model;

import com.flipkart.ranger.http.ResourceHelper;
import org.junit.Assert;
import org.junit.Test;

public class ServiceRegistrationResponseTest {

    @Test
    public void testServiceRegistrationResponse(){
        ServiceRegistrationResponse resource = ResourceHelper.getResource("fixtures/serviceResponse.json", ServiceRegistrationResponse.class);
        Assert.assertNotNull(resource);
        Assert.assertTrue(resource.isSuccess());
    }
}
