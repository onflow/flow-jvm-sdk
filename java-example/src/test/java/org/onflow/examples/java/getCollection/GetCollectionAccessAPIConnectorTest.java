package org.onflow.examples.java.getCollection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.examples.java.AccessAPIConnector;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowServiceAccountCredentials;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.common.test.TestAccount;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class GetCollectionAccessAPIConnectorTest {
    @FlowServiceAccountCredentials
    private TestAccount serviceAccount;
    @FlowTestClient
    private FlowAccessApi accessAPI;
    private GetCollectionAccessAPIConnector connector;
    private FlowId collectionId;

    @BeforeEach
    public void setup() {
        AccessAPIConnector accessAPIConnector = new AccessAPIConnector(serviceAccount.getPrivateKey(), accessAPI);
        connector = new GetCollectionAccessAPIConnector(accessAPI);

        // Send a sample transaction
        PublicKey publicKey = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256).getPublic();
        accessAPIConnector.sendSampleTransaction(
            serviceAccount.getFlowAddress(),
            publicKey
        );

        FlowAccessApi.AccessApiCallResponse<FlowBlock> response = accessAPI.getLatestBlock(true, false);
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
