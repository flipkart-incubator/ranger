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
package com.flipkart.ranger.zk.server.bundle.resources;

import com.codahale.metrics.annotation.Metered;
import com.flipkart.ranger.client.RangerHubClient;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.http.response.model.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.flipkart.ranger.http.response.model.RangerResponseCode.SUCCESS;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/ranger")
public class RangerResource<T> {

    private final List<RangerHubClient<T>> rangerHubs;

    @Inject
    public RangerResource(List<RangerHubClient<T>> rangerHubs){
        this.rangerHubs = rangerHubs;
    }

    @GET
    @Path("/services/v1")
    @Metered
    public GenericResponse<Set<Service>> getServices() {
        return GenericResponse.<Set<Service>>builder()
                .code(SUCCESS)
                .data(rangerHubs.stream().map(RangerHubClient::getRegisteredServices)
                        .flatMap(Collection::stream).collect(Collectors.toSet()))
                .build();
    }

    @GET
    @Path("/nodes/v1/{namespace}/{serviceName}")
    @Metered
    public GenericResponse<List<ServiceNode<T>>> getNodes(
            @NotNull @NotEmpty @PathParam("namespace") final String namespace,
            @NotNull @NotEmpty @PathParam("serviceName") final String serviceName
    ){
        val service = Service.builder().namespace(namespace).serviceName(serviceName).build();
        return GenericResponse.<List<ServiceNode<T>>>builder()
                .code(SUCCESS)
                .data(rangerHubs.stream().map(hub -> hub.getAllNodes(service))
                        .flatMap(List::stream).collect(Collectors.toList()))
                .build();
    }
}
