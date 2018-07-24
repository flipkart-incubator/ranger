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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;

public class HttpServiceRegistryUpdater<T> extends AbstractServiceRegistryUpdater<T> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    private ServiceRegistry<T> serviceRegistry;
    private HttpService service;

    protected HttpServiceRegistryUpdater(ServiceRegistry<T> serviceRegistry, HttpService service){
        super(serviceRegistry);
        //TODO: think of a better way to assign serviceRegistry
        this.serviceRegistry = serviceRegistry;
        this.service = service;
    }

    @Override
    public void start() throws Exception {
        //TODO

        serviceRegistry.nodes(getServiceNodes());
        logger.info("Started polling zookeeper for changes");
    }

    @Override
    public void stop() {
        logger.debug("Stopped updater");
    }

    @Override
    protected List<ServiceNode<T>> getServiceNodes() {
        try{
            final long healthcheckZombieCheckThresholdTime = System.currentTimeMillis() - 60000; //1 Minute
            //final Service service = serviceRegistry.getService();
            final Deserializer<T> deserializer = serviceRegistry.getDeserializer();

            //my understanding =>
            //send the get request and get the data
            final URI uri = service.getURI();

            //TODO: where to have this HttpClient
            CloseableHttpClient httpclient = HttpClients.createDefault();
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
