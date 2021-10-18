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
package com.flipkart.ranger.server.resources;

import com.codahale.metrics.annotation.Metered;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.server.manager.RangerClientManager;
import com.flipkart.ranger.server.model.GenericResponse;
import com.flipkart.ranger.server.model.ShardInfo;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/ranger")
public class RangerResource {

    private final RangerClientManager clientManager;

    @Inject
    public RangerResource(RangerClientManager clientManager){
        this.clientManager = clientManager;
    }

    @GET
    @Path("/services/v1")
    @Metered
    public GenericResponse<Collection<Service>> getServices() {
        return GenericResponse.<Collection<Service>>builder()
                .success(true)
                .data(clientManager.getHubClient().getServices())
                .build();
    }

    @GET
    @Path("/nodes/v1/{namespace}/{serviceName}")
    @Metered
    public GenericResponse<List<ServiceNode<ShardInfo>>> getNodes(
            @NotNull @NotEmpty @PathParam("namespace") final String namespace,
            @NotNull @NotEmpty @PathParam("serviceName") final String serviceName
    ){
        val service = new Service(namespace, serviceName);
        Optional<List<ServiceNode<ShardInfo>>> nodeList = clientManager.getHubClient().getAllNodes(
                service, null);
        return GenericResponse.<List<ServiceNode<ShardInfo>>>builder()
                .success(true)
                .data(nodeList.orElse(Collections.emptyList()))
                .build();
    }
}
