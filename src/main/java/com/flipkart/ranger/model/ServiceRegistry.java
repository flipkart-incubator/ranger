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

package com.flipkart.ranger.model;

import java.util.List;

public interface ServiceRegistry<T> {
//    private final Service service;
//    public Deserializer<T> deserializer;

//    protected ServiceRegistry(Deserializer<T> deserializer) {
////        this.service = service;
//        this.deserializer = deserializer;
//    }

//    public abstract void start() throws Exception;

//    public abstract void stop() throws Exception;

    public void nodes(List<ServiceNode<T>> nodes);

//    public Service getService() {
//        return service;
//    }

//    public Deserializer<T> getDeserializer() {
//        return deserializer;
//    }
}
