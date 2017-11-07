/**
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

package com.flipkart.ranger.finder;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ActiveConnectionMetrics {

    private static final Logger log = LoggerFactory.getLogger(ActiveConnectionMetrics.class);

    // this map stores the number of active connections to various replicas at any point of time
    private static final Map<ConnectionRequest, AtomicLong> activeConnections = Maps.newConcurrentMap();

    public static void incrementConnectionCount(ConnectionRequest connectionRequest, String requestId) {
        Preconditions.checkNotNull(connectionRequest, "connectionRequestCannot be null");
        if (!activeConnections.containsKey(connectionRequest)) {
            activeConnections.put(connectionRequest, new AtomicLong(1));
            return;
        }
        log.debug("RequestId = {}, INCREMENTED connection count for {} = {}", requestId, connectionRequest,
                activeConnections.get(connectionRequest).incrementAndGet());
    }

    public static void decrementConnectionCount(ConnectionRequest connectionRequest, String requestId) {
        Preconditions.checkNotNull(connectionRequest, "connectionRequestCannot be null");
        if (!activeConnections.containsKey(connectionRequest)) {
            String errorMsg = "connectionRequest " + connectionRequest + " should be already present in activeConnections map";
            log.error(errorMsg);
            return;
        }
        log.debug("RequestId = {}, DECREMENTED connection count for {} = {}", requestId, connectionRequest,
                activeConnections.get(connectionRequest).decrementAndGet());
    }

    public static Map<ConnectionRequest, AtomicLong> getActiveConnections() {
        return ImmutableMap.copyOf(activeConnections);
    }
}
