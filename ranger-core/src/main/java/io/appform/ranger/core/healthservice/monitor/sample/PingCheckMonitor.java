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
package io.appform.ranger.core.healthservice.monitor.sample;

import io.appform.ranger.core.healthcheck.HealthcheckStatus;
import io.appform.ranger.core.healthservice.TimeEntity;
import io.appform.ranger.core.healthservice.monitor.IsolatedHealthMonitor;
import io.appform.ranger.core.healthservice.monitor.RollingWindowHealthQueue;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.concurrent.*;

/**
 * A Ping checking monitor, which executes a {@link HttpRequest} at regular intervals
 * Maintains every healthcheck in a {@link RollingWindowHealthQueue} to prevent continuous flaps of health
 */
@Slf4j
public class PingCheckMonitor extends IsolatedHealthMonitor<HealthcheckStatus> {
    
    private HttpRequest httpRequest;
    private String host;
    private ExecutorService executorService;
    private Integer pingTimeoutInMilliseconds;
    private CloseableHttpClient httpClient;
    private int port;
    private RollingWindowHealthQueue rollingWindowHealthQueue;

    /**
     * @param timeEntity                how often the {@link #monitor()} check needs to be executed
     * @param httpRequest               http request that will be called at regular intervals
     * @param pingTimeoutInMilliseconds timeout in milliseconds for http request execution (ping response)
     * @param pingWindowSize            rolling window frame, which needs to be maintained
     * @param maxFailures               maximum failures allowed in the rolling window frame
     * @param host                      host name (could be localhost)
     * @param port                      port
     */
    public PingCheckMonitor(
            TimeEntity timeEntity,
            HttpRequest httpRequest,
            Integer pingTimeoutInMilliseconds,
            Integer pingWindowSize,
            Integer maxFailures,
            String host,
            Integer port) {
        super(PingCheckMonitor.class.getSimpleName(), timeEntity);
        this.httpRequest = httpRequest;
        this.pingTimeoutInMilliseconds = pingTimeoutInMilliseconds;
        this.host = host;
        this.port = port;
        this.rollingWindowHealthQueue = new RollingWindowHealthQueue(pingWindowSize, maxFailures);
        this.executorService = Executors.newSingleThreadExecutor();
        val connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(host, port)), 2);
        this.httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
    }

    @Override
    public HealthcheckStatus monitor() {
        log.debug("Running ping monitor :{} with HttpRequest:{} on host:{} port:{}", name, httpRequest, host, port);
        val futurePingResponse = executorService.submit(this::healthPing);

        try {
            val pingSuccessful = futurePingResponse.get(pingTimeoutInMilliseconds, TimeUnit.MILLISECONDS);
            return getRollingWindowHealthcheckStatus(
                    pingSuccessful ? HealthcheckStatus.healthy : HealthcheckStatus.unhealthy
            );
        } catch (InterruptedException e) {
            log.error("Request thread interrupted");
            Thread.currentThread().interrupt();
            return getRollingWindowHealthcheckStatus(HealthcheckStatus.unhealthy);
        }
        catch (ExecutionException | TimeoutException e) {
            log.error("Ping monitor failed:{} with HttpRequest:{} on host:{} port:{}", name, httpRequest, host, port);
            log.error("Error running ping monitor: ", e);
            return getRollingWindowHealthcheckStatus(HealthcheckStatus.unhealthy);
        }
    }

    private HealthcheckStatus getRollingWindowHealthcheckStatus(HealthcheckStatus healthy) {
        if (rollingWindowHealthQueue.checkInRollingWindow(healthy)) {
            return HealthcheckStatus.healthy;
        } else {
            log.info("{} is marking itself unhealthy since the current rolling window frame contains many failures (> threshold)). " +
                    "Was pinging on HttpRequest:{} on host:{} port:{}", name, httpRequest, host, port);
            return HealthcheckStatus.unhealthy;
        }
    }

    private boolean healthPing() {
        try {
            log.debug("executing http HttpRequest: {}, host:{}, port:{}", httpRequest, host, port);
            val response = httpClient.execute(new HttpHost(host, port), httpRequest);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.error("Error while executing Ping Test. HttpRequest: {}, host:{}, port:{}, reason:{}", httpRequest, host, port, response.getStatusLine().getReasonPhrase());
                response.close();
                return false;
            }
            response.close();
            return true;
        } catch (Exception e) {
            log.error("Exception while executing HttpRequest: ", e);
            return false;
        }
    }
}
