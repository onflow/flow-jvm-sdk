package org.onflow.examples.java.getNodeVersionInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowNodeVersionInfo;

import static org.junit.jupiter.api.Assertions.*;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class GetNodeVersionInfoAccessAPIConnectorTest {
    @FlowTestClient
    private FlowAccessApi accessAPI;
    private GetNodeVersionInfoAccessAPIConnector nodeVersionInfoConnector;
    @BeforeEach
    public void setup() {
        nodeVersionInfoConnector = new GetNodeVersionInfoAccessAPIConnector(accessAPI);
    }

    @Test
    public void canFetchNodeVersionInfo() {
        FlowNodeVersionInfo nodeVersionInfo = nodeVersionInfoConnector.getNodeVersionInfo();
        assertNotNull(nodeVersionInfo, "Node version info should not be null");
        assertEquals(nodeVersionInfo.getProtocolVersion(), 0);
        assertEquals(nodeVersionInfo.getSporkRootBlockHeight(), 0);
        assertEquals(nodeVersionInfo.getNodeRootBlockHeight(), 0);
        assertNull(nodeVersionInfo.getCompatibleRange());
    }
}
