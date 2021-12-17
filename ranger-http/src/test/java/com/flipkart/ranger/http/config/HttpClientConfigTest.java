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
package com.flipkart.ranger.http.config;

import com.flipkart.ranger.http.ResourceHelper;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

public class HttpClientConfigTest {

    @Test
    public void testHttpClientConfig(){
        val resource = ResourceHelper.getResource("fixtures/httpClientConfig.json", HttpClientConfig.class);
        Assert.assertEquals("localhost-1", resource.getHost());
        Assert.assertEquals(80, resource.getPort());
        Assert.assertEquals(10, resource.getConnectionTimeoutMs());
        Assert.assertEquals(10, resource.getOperationTimeoutMs());
    }
}
