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

package io.appform.ranger.core.serviceprovider;

import io.appform.ranger.core.healthcheck.HealthcheckResult;
import io.appform.ranger.core.model.NodeDataSink;
import io.appform.ranger.core.model.Serializer;
import io.appform.ranger.core.model.Service;
import io.appform.ranger.core.model.ServiceNode;
import io.appform.ranger.core.signals.ExternalTriggeredSignal;
import io.appform.ranger.core.signals.Signal;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
public class ServiceProvider<T, S extends Serializer<T>> {

    private final Service service;
    private final ServiceNode<T> serviceNode;
    private final S serializer;
    private final NodeDataSink<T, S> dataSink;
    @Getter
    private final ExternalTriggeredSignal<Void> startSignal = new ExternalTriggeredSignal<>(() -> null, Collections.emptyList());
    @Getter
    private final ExternalTriggeredSignal<Void> stopSignal = new ExternalTriggeredSignal<>(() -> null, Collections.emptyList());

    public ServiceProvider(
            Service service,
            ServiceNode<T> serviceNode,
            S serializer,
            NodeDataSink<T, S> dataSink,
            List<Signal<HealthcheckResult>> signalGenerators) {
        this.service = service;
        this.serviceNode = serviceNode;
        this.serializer = serializer;
        this.dataSink = dataSink;
        signalGenerators.forEach(signalGenerator -> signalGenerator.registerConsumer(this::handleHealthUpdate));
    }

    public void start() {
        startSignal.trigger();
        dataSink.updateState(serializer, serviceNode);
        log.debug("Set initial node data on zookeeper for {}", service.getServiceName());
    }

    public void stop() {
        stopSignal.trigger();
    }

    private void handleHealthUpdate(HealthcheckResult result) {
        if(null == result) {
            log.debug("No update to health state of node. Skipping data source update.");
            return;
        }
        serviceNode.setHealthcheckStatus(result.getStatus());
        serviceNode.setLastUpdatedTimeStamp(result.getUpdatedTime());
        dataSink.updateState(serializer, serviceNode);
        log.debug("Updated node with health check result: {}", result);
    }

}
