package com.flipkart.ranger.healthservice.monitor.sample;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.healthservice.TimeEntity;
import com.flipkart.ranger.healthservice.monitor.IsolatedHealthMonitor;
import com.flipkart.ranger.healthservice.monitor.RollingWindowHealthQueue;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * A Ping checking monitor, which executes a {@link HttpRequest} at regular intervals
 * Maintains every healthcheck in a {@link RollingWindowHealthQueue} to prevent continuous flaps of health
 */
public class PingCheckMonitor extends IsolatedHealthMonitor {

    private static final Logger logger = LoggerFactory.getLogger(PingCheckMonitor.class.getSimpleName());

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
    public PingCheckMonitor(TimeEntity timeEntity,
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
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(host, port)), 2);
        this.httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
    }

    @Override
    public HealthcheckStatus monitor() {
        logger.debug("Running ping monitor :{} with HttpRequest:{} on host:{} port:{}", name, httpRequest, host, port);
        Future<Boolean> futurePingResponse = executorService.submit(new Callable<Boolean>() {
            public Boolean call() {
                return healthPing();
            }
        });

        try {
            final Boolean pingSuccessful = futurePingResponse.get(pingTimeoutInMilliseconds, TimeUnit.MILLISECONDS);
            if (!pingSuccessful) {
                return getRollingWindowHealthcheckStatus(HealthcheckStatus.unhealthy);
            } else {
                return getRollingWindowHealthcheckStatus(HealthcheckStatus.healthy);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Ping monitor failed:{} with HttpRequest:{} on host:{} port:{}", name, httpRequest, host, port, e);
            return getRollingWindowHealthcheckStatus(HealthcheckStatus.unhealthy);
        }
    }

    private HealthcheckStatus getRollingWindowHealthcheckStatus(HealthcheckStatus healthy) {
        if (rollingWindowHealthQueue.checkInRollingWindow(healthy)) {
            return HealthcheckStatus.healthy;
        } else {
            logger.info("{} is marking itself unhealthy since the current rolling window frame contains many failures (> threshold)). " +
                    "Was pinging on HttpRequest:{} on host:{} port:{}", name, httpRequest, host, port);
            return HealthcheckStatus.unhealthy;
        }
    }

    private boolean healthPing() {
        try {
            logger.debug("executing http HttpRequest: {}, host:{}, port:{}", httpRequest, host, port);
            CloseableHttpResponse response = httpClient.execute(new HttpHost(host, port), httpRequest);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("Error while executing Ping Test. HttpRequest: {}, host:{}, port:{}, reason:{}", httpRequest, host, port, response.getStatusLine().getReasonPhrase());
                response.close();
                return false;
            }
            response.close();
            return true;
        } catch (Exception e) {
            logger.error("Exception while executing HttpRequest: ", e);
            return false;
        }
    }
}
