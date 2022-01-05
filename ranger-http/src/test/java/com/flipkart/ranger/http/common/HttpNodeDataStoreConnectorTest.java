/*
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.ranger.http.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.http.config.HttpClientConfig;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

public class HttpNodeDataStoreConnectorTest {
    
    @Test
    public void testHttpNodeDataStoreConnector(){
        val objectMapper = new ObjectMapper();
        val httpClientConfig = HttpClientConfig.builder()
                .host("localhost-1")
                .port(80)
                .build();
        val httpNodeDataStoreConnector = new HttpNodeDataStoreConnector<>(httpClientConfig, objectMapper);
        Assert.assertNotNull(httpNodeDataStoreConnector);
        Assert.assertTrue(httpNodeDataStoreConnector.isActive());
    }
}
