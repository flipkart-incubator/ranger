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
package com.flipkart.ranger.core.model;

import com.flipkart.ranger.core.healthcheck.HealthcheckStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceNode<T> {
    private String host;
    private int port;
    private T nodeData;
    private HealthcheckStatus healthcheckStatus = HealthcheckStatus.healthy;
    private long lastUpdatedTimeStamp = Long.MIN_VALUE;

    public String representation() {
        return String.format("%s:%d", host, port);
    }
}
