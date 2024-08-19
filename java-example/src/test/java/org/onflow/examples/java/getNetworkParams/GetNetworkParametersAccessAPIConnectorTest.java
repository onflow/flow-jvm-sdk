package org.onflow.examples.java.getNetworkParams;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowChainId;

import static org.junit.jupiter.api.Assertions.*;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class GetNetworkParametersAPIConnectorTest {

    @FlowTestClient
    private FlowAccessApi accessAPI;

    private GetNetworkParametersAccessAPIConnector networkParametersConnector;

    @BeforeEach
    public void setup() {
        networkParametersConnector = new GetNetworkParametersAccessAPIConnector(accessAPI);
    }

    @Test
    public void canFetchNetworkParameters() {
        FlowChainId networkParams = networkParametersConnector.getNetworkParameters();
        assertNotNull(networkParams, "Network parameters should not be null");
        assertFalse(networkParams.getId().isEmpty(), "Network parameters should have a valid ID");
        assertEquals(FlowChainId.EMULATOR, networkParams, "Network parameters should match EMULATOR");
        assertEquals("flow-emulator", networkParams.getId(), "Network ID should be 'flow-emulator'");
    }
}
