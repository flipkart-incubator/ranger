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

import com.flipkart.ranger.finder.HttpVerb.HttpVerb;
import com.flipkart.ranger.model.HttpResponseDecoder;

public class HttpSourceConfig<T> extends SourceConfig<T> {
    private String host;
    private int port = 80;
    private String path;
    private boolean secure = false;

    private boolean suppressHostCheck = false;
    private HttpVerb httpVerb;

    private HttpResponseDecoder<T> httpResponseDecoder;

    public HttpSourceConfig(String host, int port, String path, HttpResponseDecoder<T> httpResponseDecoder, boolean secure, boolean suppressHostCheck, HttpVerb httpVerb) {
        super(ServiceType.HTTPSOURCE);
        this.host = host;
        this.port = port;
        this.path = path;
        this.httpResponseDecoder = httpResponseDecoder;
        this.secure = secure;
        this.httpVerb = httpVerb;
        this.suppressHostCheck = suppressHostCheck;
    }

    public HttpResponseDecoder<T> getHttpResponseDecoder() {
        return httpResponseDecoder;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public boolean isSecure() {
        return secure;
    }

    public boolean isSuppressHostCheck() {
        return suppressHostCheck;
    }

    public HttpVerb getHttpVerb() {
        return httpVerb;
    }

    public ServiceRegistryUpdater<T> accept(SourceConfigVisitor<T> sourceConfigVisitor) throws Exception {
        return sourceConfigVisitor.visit(this);
    }
}
