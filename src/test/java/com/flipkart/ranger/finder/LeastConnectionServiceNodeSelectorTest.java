package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.ServiceNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ActiveConnectionMetrics.class)
public class LeastConnectionServiceNodeSelectorTest {

    private List<ServiceNode<String>> serviceNodes;
    private LeastConnectionServiceNodeSelector<String> serviceNodeSelector;
    private Map<ConnectionRequest, AtomicLong> emptyMap = Maps.newConcurrentMap();

    @Before
    public void setUp() throws Exception {
        mockStatic(ActiveConnectionMetrics.class);
        MockitoAnnotations.initMocks(this);
        serviceNodes = getServiceNodes();
        serviceNodeSelector = new LeastConnectionServiceNodeSelector<>();
    }

    private List<ServiceNode<String>> getServiceNodes() {
        ServiceNode<String> a = new ServiceNode<>("10.85.23.1", 28231, "Node A");
        ServiceNode<String> b = new ServiceNode<>("10.85.24.1", 28241, "Node B");
        ServiceNode<String> c = new ServiceNode<>("10.85.25.1", 28251, "Node C");
        return Lists.newArrayList(a, b, c);
    }

    private Map<ConnectionRequest, AtomicLong> getActiveConnections() {
        Map<ConnectionRequest, AtomicLong> activeConnections = Maps.newConcurrentMap();
        ConnectionRequest c1 = new ConnectionRequest("electronicsLow-shard3", "10.85.23.1", 28231);
        ConnectionRequest c2 = new ConnectionRequest("electronicsLow-shard3", "10.85.24.1", 28241);
        ConnectionRequest c3 = new ConnectionRequest("electronicsLow-shard3", "10.85.25.1", 28251);
        activeConnections.put(c1, new AtomicLong(10));
        activeConnections.put(c2, new AtomicLong(5));
        activeConnections.put(c3, new AtomicLong(15));

        ConnectionRequest c4 = new ConnectionRequest("lifestyleCore-shard1", "10.85.23.2", 28232);
        ConnectionRequest c5 = new ConnectionRequest("lifestyleCore-shard1", "10.85.24.2", 28242);
        ConnectionRequest c6 = new ConnectionRequest("lifestyleCore-shard1", "10.85.25.2", 28252);
        activeConnections.put(c4, new AtomicLong(23));
        activeConnections.put(c5, new AtomicLong(26));
        activeConnections.put(c6, new AtomicLong(10));

        ConnectionRequest c7 = new ConnectionRequest("booksCore-shard4", "10.85.23.3", 28233);
        ConnectionRequest c8 = new ConnectionRequest("lifestyleCore-shard5", "10.85.24.3", 28243);
        ConnectionRequest c9 = new ConnectionRequest("electronicsHigh-shard4", "10.85.25.3", 28253);
        activeConnections.put(c7, new AtomicLong(11));
        activeConnections.put(c8, new AtomicLong(3));
        activeConnections.put(c9, new AtomicLong(6));

        return activeConnections;
    }

    /**
     * Case: This test case simulates cases when activeConnections map has only one entry for respective service node
     * and there as multiple service nodes to select from.
     * Output: Nodes should be selected using Random algorithm.
     */
    @Test
    public void selectServiceNodeWhenOnlyOneActiveConnection() throws Exception {
        Map<ConnectionRequest, AtomicLong> activeConnections = Maps.newConcurrentMap();
        ConnectionRequest c1 = new ConnectionRequest("electronicsLow-shard3", "10.85.25.1", 28251);
        activeConnections.put(c1, new AtomicLong(10));

        when(ActiveConnectionMetrics.getActiveConnections()).thenReturn(activeConnections, activeConnections, activeConnections,
                activeConnections);

        ServiceNode<String> serviceNode;
        serviceNode = serviceNodeSelector.select(serviceNodes);
        assertNotNull(serviceNode);
    }

    @Test
    /**
     * Output: Service node having least number of connections.
     */
    public void selectLeastConnectionServiceNode() throws Exception {
        when(ActiveConnectionMetrics.getActiveConnections()).thenReturn(getActiveConnections());
        ServiceNode<String> serviceNode = serviceNodeSelector.select(serviceNodes);
        assertEquals(28241, serviceNode.getPort());
        assertEquals("10.85.24.1", serviceNode.getHost());
    }


}