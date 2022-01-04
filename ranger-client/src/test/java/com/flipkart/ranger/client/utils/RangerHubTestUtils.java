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
package com.flipkart.ranger.client.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.client.stubs.RangerTestHub;
import com.flipkart.ranger.client.stubs.TestCriteria;
import com.flipkart.ranger.client.stubs.TestDeserializer;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.utils.RangerTestUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RangerHubTestUtils {

    public static final Service service = RangerTestUtils.getService("test-ns", "test-s");
    private static final ObjectMapper mapper = new ObjectMapper();

    public static RangerTestHub getTestHub(){
        return RangerTestHub.builder()
                .namespace(service.getNamespace())
                .mapper(mapper)
                .nodeRefreshTimeMs(1000)
                .criteria(new TestCriteria())
                .deserializer(new TestDeserializer<>())
                .build();
    }

}
