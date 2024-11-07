package org.onflow.examples.java.getProtocolState;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowBlock;
import org.onflow.flow.sdk.FlowSnapshot;

import static org.junit.jupiter.api.Assertions.*;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class GetProtocolStateAccessAPIConnectorTest {
    @FlowTestClient
    private FlowAccessApi accessAPI;
    private FlowBlock block;

    private GetProtocolStateAccessAPIConnector protocolStateConnector;

    @BeforeEach
    public void setup() {
        protocolStateConnector = new GetProtocolStateAccessAPIConnector(accessAPI);
    }

    @Test
    public void canGetLatestProtocolStateSnapshot() {
        FlowSnapshot latestSnapshot = protocolStateConnector.getLatestProtocolStateSnapshot();
        assertNotNull(latestSnapshot, "Latest snapshot should not be null");
    }

    @Test
    public void canGetProtocolStateSnapshotByBlockId() {
        block = getLatestBlock();
        FlowSnapshot snapshot = protocolStateConnector.getProtocolStateSnapshotByBlockId(block.getId());
        assertNotNull(snapshot, "Snapshot should not be null");
    }

    @Test
    public void canGetProtocolStateSnapshotByHeight() {
        block = getLatestBlock();
        FlowSnapshot snapshot = protocolStateConnector.getProtocolStateSnapshotByHeight(block.getHeight());
        assertNotNull(snapshot, "Snapshot should not be null");
    }

    private FlowBlock getLatestBlock() {
        FlowAccessApi.AccessApiCallResponse<FlowBlock> response = accessAPI.getLatestBlock(true, false);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            return ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlock>) response).getData();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }
}
