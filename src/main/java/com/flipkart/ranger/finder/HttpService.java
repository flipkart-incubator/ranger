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

import java.net.URI;

public class HttpService extends Service{
    private static final Logger logger = LoggerFactory.getLogger(CuratorService.class);
    //private String urlPath;
    private URI uri;

    public HttpService(URI uri) {
        super(ServiceType.HTTP);
        //this.urlPath = urlPath;
        this.uri = uri;
    }

//    public String getUrlPath() {
//        return urlPath;
//    }

    public URI getURI(){
        return uri;
    }

    //TODO: verify if we need the setter as we are setting urlPath via constructor
//    public void setUrlPath(String urlPath) {
//        this.urlPath = urlPath;
//    }

    public <T> T accept(ServiceVisitor<T> serviceVisitor) {
        return serviceVisitor.visit(this);
    }

    public void start() throws Exception{
        logger.debug("Starting Http Service");
    }

    public void stop() throws Exception{
        logger.debug("Stopping Curator Service");
    }

}
