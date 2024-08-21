package org.onflow.examples.java.getExecutionData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.sdk.*;

import static org.junit.jupiter.api.Assertions.*;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class GetExecutionDataAccessAPIConnectorTest {

    @FlowTestClient
    private FlowAccessApi accessAPI;

    private GetExecutionDataAccessAPIConnector connector;
    private FlowId blockId;

    @BeforeEach
    public void setup() {
        connector = new GetExecutionDataAccessAPIConnector(accessAPI);

        // Set a specific block ID
        FlowBlock block;
        FlowAccessApi.AccessApiCallResponse<FlowBlock> response = accessAPI.getLatestBlock(true);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            block = ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlock>) response).getData();
        } else {
            throw new RuntimeException(((FlowAccessApi.AccessApiCallResponse.Error) response).getMessage(), ((FlowAccessApi.AccessApiCallResponse.Error) response).getThrowable());
        }
        blockId = block.getId();
    }

    @Test
    public void canFetchExecutionDataByBlockId() {
        FlowExecutionResult executionData = connector.getExecutionDataByBlockId(blockId);

        assertNotNull(executionData, "Execution data should not be null");

        assertFalse(executionData.getChunks().isEmpty(), "Execution data should contain chunks");

        for (int chunkNo = 0; chunkNo < executionData.getChunks().size(); chunkNo++) {
            FlowChunk chunk = executionData.getChunks().get(chunkNo);
            assertTrue(chunk.getNumberOfTransactions() > 0, "Chunk " + chunkNo + " should contain transactions");
        }
    }
}
