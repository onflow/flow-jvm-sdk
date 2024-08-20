package org.onflow.examples.java.getCollection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.sdk.FlowAccessApi;
import org.onflow.flow.sdk.FlowBlock;
import org.onflow.flow.sdk.FlowCollection;
import org.onflow.flow.sdk.FlowId;

import static org.junit.jupiter.api.Assertions.*;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class GetCollectionAccessAPIConnectorTest {
    @FlowTestClient
    private FlowAccessApi accessAPI;

    private GetCollectionAccessAPIConnector connector;
    private FlowId collectionId;

    @BeforeEach
    public void setup() {
        connector = new GetCollectionAccessAPIConnector(accessAPI);

        FlowAccessApi.AccessApiCallResponse<FlowBlock> response = accessAPI.getLatestBlock(true);
        if (response instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            FlowBlock block = ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlock>) response).getData();
            collectionId = block.getCollectionGuarantees().get(0).getId();
        } else {
            FlowAccessApi.AccessApiCallResponse.Error errorResponse = (FlowAccessApi.AccessApiCallResponse.Error) response;
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        }
    }

    @Test
    public void canFetchCollectionById() {
        FlowCollection collection = connector.getCollectionById(collectionId);

        assertNotNull(collection, "Collection should not be null");
        assertEquals(collectionId, collection.getId(), "Collection ID should match the fetched collection ID");
    }
}
