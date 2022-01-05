/*
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
package io.appform.ranger.zk.server.bundle;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.appform.ranger.client.RangerHubClient;
import io.appform.ranger.client.stubs.RangerTestHub;
import io.appform.ranger.client.utils.RangerHubTestUtils;
import io.appform.ranger.core.units.TestNodeData;
import io.appform.ranger.core.utils.RangerTestUtils;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.AdminEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.val;
import lombok.var;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static io.appform.ranger.client.utils.RangerHubTestUtils.service;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RangerServerBundleTest {

    private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
    private final MetricRegistry metricRegistry = mock(MetricRegistry.class);
    private final LifecycleEnvironment lifecycleEnvironment = new LifecycleEnvironment(metricRegistry);
    private final Environment environment = mock(Environment.class);
    private final Bootstrap<?> bootstrap = mock(Bootstrap.class);
    private final Configuration configuration = mock(Configuration.class);

    private final RangerServerBundle<TestNodeData, Configuration>
            rangerServerBundle = new RangerServerBundle<TestNodeData, Configuration>() {

        @Override
        protected List<RangerHubClient<TestNodeData>> withHubs(Configuration configuration) {
            return Collections.singletonList(RangerHubTestUtils.getTestHub());
        }

        @Override
        protected List<HealthCheck> withHealthChecks(Configuration configuration) {
            return Collections.emptyList();
        }
    };

    @Before
    public void setup() throws Exception {
        when(jerseyEnvironment.getResourceConfig()).thenReturn(new DropwizardResourceConfig());
        when(environment.jersey()).thenReturn(jerseyEnvironment);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.getObjectMapper()).thenReturn(new ObjectMapper());
        AdminEnvironment adminEnvironment = mock(AdminEnvironment.class);
        doNothing().when(adminEnvironment).addTask(any());
        when(environment.admin()).thenReturn(adminEnvironment);

        rangerServerBundle.initialize(bootstrap);
        rangerServerBundle.run(configuration, environment);
        for (val lifeCycle : lifecycleEnvironment.getManagedObjects()){
            lifeCycle.start();
        }
    }


    @Test
    public void testRangerBundle(){
        var hub = rangerServerBundle.getHubs().get(0);
        Assert.assertTrue(hub instanceof RangerTestHub);
        RangerTestUtils.sleepUntilHubStarts(((RangerTestHub) hub).getHub());
        var node = hub.getNode(service).orElse(null);
        Assert.assertNotNull(node);
        Assert.assertTrue(node.getHost().equalsIgnoreCase("localhost"));
        Assert.assertEquals(9200, node.getPort());
        Assert.assertEquals(1, node.getNodeData().getShardId());
        Assert.assertNull(hub.getNode(RangerTestUtils.getService("test", "test")).orElse(null));
        Assert.assertNull(hub.getNode(service, nodeData -> nodeData.getShardId() == 2).orElse(null));
        Assert.assertNull(hub.getNode(RangerTestUtils.getService("test", "test"), nodeData -> nodeData.getShardId() == 1).orElse(null));
    }

    @After
    public void tearDown() throws Exception {
        for (LifeCycle lifeCycle: lifecycleEnvironment.getManagedObjects()){
            lifeCycle.stop();
        }
    }
}
