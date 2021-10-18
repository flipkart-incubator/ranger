/*
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.ranger.server.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShardInfo {
    private String environment;
    private String region;

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof ShardInfo))
            return false;

        ShardInfo other = (ShardInfo) o;
        return Objects.equals(this.getEnvironment(), other.getEnvironment())
                && Objects.equals(this.getRegion(), other.getRegion());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = hash + (this.environment != null ? this.environment.hashCode() : 0);
        hash = hash + (this.region != null ? this.region.hashCode() : 0);
        return hash;
    }
}
