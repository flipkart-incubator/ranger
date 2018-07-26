package com.flipkart.ranger.finder;

import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.model.ServiceRegistry;
import com.google.common.collect.Lists;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;

public class HttpServiceRegistryUpdater<T> extends AbstractServiceRegistryUpdater<T> {
    private static final Logger logger = LoggerFactory.getLogger(HttpServiceRegistryUpdater.class);

    private HttpSourceConfig config;
    private CloseableHttpClient httpclient;
    private Deserializer<T> deserializer;

    protected HttpServiceRegistryUpdater(HttpSourceConfig config, Deserializer<T> deserializer){
        this.config = config;
        this.deserializer = deserializer;
    }

    @Override
    public void start() throws Exception {
        //TODO: where to have this HttpClient
        httpclient = HttpClients.createDefault();

        serviceRegistry.nodes(getHealthyServiceNodes());
        logger.info("Started polling zookeeper for changes");
    }

    @Override
    public void stop() throws Exception{
        httpclient.close();
        logger.debug("Stopped updater");
    }

    @Override
    protected List<ServiceNode<T>> getHealthyServiceNodes() {
        try{
            final long healthcheckZombieCheckThresholdTime = System.currentTimeMillis() - 60000; //1 Minute

            //my understanding =>
            //send the get request and get the data
            final String host = config.getHost();
            final Integer port = config.getPort();
            final String path = config.getPath();
            final URI uri = new URIBuilder()
                .setScheme("http")
                .setHost(host)
                .setPort(port)
                .setPath(path)
                .build();

            HttpGet httpget = new HttpGet(uri);
            CloseableHttpResponse response = httpclient.execute(httpget);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("Error in Http get");
                response.close();
                return null;
            }
            HttpEntity entity = response.getEntity();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entity.writeTo(baos);
            try {
                byte[] data = baos.toByteArray();
                List<String> children = null;
                List<ServiceNode<T>> nodes = Lists.newArrayListWithCapacity(children.size());

                // TODO: for each ServiceNode
                if(null == data) {
                    logger.warn("No data present");
                    //continue;
                }
                ServiceNode<T> key = deserializer.deserialize(data);
                //TODO: is HealthcheckStatus needed for http?
                if(HealthcheckStatus.healthy == key.getHealthcheckStatus()
                        && key.getLastUpdatedTimeStamp() > healthcheckZombieCheckThresholdTime) {
                    nodes.add(key);
                }
                //for ends
            } finally {
                response.close();
            }

            return null;
        } catch (Exception e) {
            logger.error("Error getting service data from http: ", e);
        }
        return null;
    }
}
