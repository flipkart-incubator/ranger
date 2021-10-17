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
package com.flipkart.ranger.client.zk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.core.finder.nodeselector.RoundRobinServiceNodeSelector;
import com.flipkart.ranger.core.finder.serviceregistry.ListBasedServiceRegistry;
import com.flipkart.ranger.core.finder.shardselector.ListShardSelector;
import com.flipkart.ranger.core.finderhub.ServiceFinderFactory;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceNode;
import com.flipkart.ranger.zookeeper.servicefinderhub.ZKUnshardedServiceFinderFactory;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.io.IOException;
import java.util.List;

@Slf4j
public class UnshardedZKHubClient<T, C extends Criteria<T>> extends AbstractZKHubClient<T, C, ListBasedServiceRegistry<T>> {

    @Builder
    public UnshardedZKHubClient(
            String namespace,
            String environment,
            ObjectMapper mapper,
            int refreshTimeMs,
            boolean disablePushUpdaters,
            String connectionString,
            CuratorFramework curatorFramework,
            C criteria,
            List<Service> services
    ) {
            super(
                    namespace,
                    environment,
                    mapper,
                    refreshTimeMs,
                    disablePushUpdaters,
                    connectionString,
                    curatorFramework,
                    criteria,
                    services
            );
    }

    @Override
    public ServiceFinderFactory<T, C, ListBasedServiceRegistry<T>> getFinderFactory() {
        return ZKUnshardedServiceFinderFactory.<T, C>builder()
            .curatorFramework(getCuratorFramework())
            .connectionString(getConnectionString())
            .nodeRefreshIntervalMs(getRefreshTimeMs())
            .disablePushUpdaters(isDisablePushUpdaters())
            .deserializer(data -> {
            try{
                return getMapper().readValue(data, new TypeReference<ServiceNode<T>>() {});
            }catch (IOException e){
                log.warn("Could not parse node data");
            }
            return null;
            })
            .shardSelector(new ListShardSelector<>())
            .nodeSelector(new RoundRobinServiceNodeSelector<>())
            .build();
    }

}
