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

package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.ListDeserializer;

public class HttpSourceConfig<T> extends SourceConfig {
    private String host;
    private Integer port;
    private String path;

    private ListDeserializer<T> listDeserializer;

    public HttpSourceConfig(String host, int port, String path, ListDeserializer<T> listDeserializer) {
        super(ServiceType.HTTP);
        this.host = host;
        this.port = port;
        this.path = path;
        this.listDeserializer = listDeserializer;
    }

    public ListDeserializer<T> getListDeserializer() {
        return listDeserializer;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }
}
