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
package com.flipkart.ranger.server.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flipkart.ranger.client.RangerClientConstants;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RangerConfiguration {
    @NotEmpty
    @NotNull
    private String namespace;
    @NotEmpty
    @NotNull
    private String zookeeper;
    private boolean disablePushUpdaters;
    @Min(1000)
    private int nodeRefreshTimeMs = RangerClientConstants.MINIMUM_REFRESH_TIME;
}
