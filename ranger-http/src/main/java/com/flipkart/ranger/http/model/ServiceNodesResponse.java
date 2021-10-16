/**
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
package com.flipkart.ranger.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.ranger.core.model.ServiceNode;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 *
 */
@Data
public class  ServiceNodesResponse<T> {
    private final boolean success;
    final List<ServiceNode<T>> nodes;

    @Builder
    public ServiceNodesResponse(
            @JsonProperty("success") boolean success,
            @JsonProperty("nodes") @Singular List<ServiceNode<T>> nodes) {
        this.success = success;
        this.nodes = nodes;
    }
}
