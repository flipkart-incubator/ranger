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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpSourceConfig extends SourceConfig{
    private static final Logger logger = LoggerFactory.getLogger(CuratorSourceConfig.class);
    private String host;
    private Integer port;
    private String path;

    public HttpSourceConfig(String host, int port, String path) {
        super(ServiceType.HTTP);
        this.host = host;
        this.port = port;
        this.path = path;
    }

    public String getHost(){
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public <T> T accept(ServiceVisitor<T> serviceVisitor) {
        return serviceVisitor.visit(this);
    }

//    public void start() throws Exception{
//        logger.debug("Starting Http Service");
//    }

//    public void stop() throws Exception{
//        logger.debug("Stopping Curator Service");
//    }

}