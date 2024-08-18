package org.onflow.examples.java.getEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.examples.java.AccessAPIConnector;
import org.onflow.flow.common.test.*;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.PublicKey;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class GetEventAccessAPIConnectorTest {
    @FlowServiceAccountCredentials
    private TestAccount serviceAccount;

    @FlowTestAccount
    private TestAccount testAccount;

    @FlowTestClient
    private FlowAccessApi accessAPI;

    private GetEventAccessAPIConnector connector;
    private AccessAPIConnector accessAPIConnector;

    private FlowId txID;

    @BeforeEach
    void setup() throws ExecutionException, InterruptedException, TimeoutException {
        accessAPIConnector = new AccessAPIConnector(serviceAccount.getPrivateKey(), accessAPI);
        connector = new GetEventAccessAPIConnector(accessAPI);

        // Send a sample transaction to create an account and capture the transaction ID
        PublicKey publicKey = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256).getPublic();
        txID = accessAPIConnector.sendSampleTransaction(
                serviceAccount.getFlowAddress(),
                publicKey
        );
    }

    @Test
    void testGetEventsForHeightRange() throws ExecutionException, InterruptedException, TimeoutException {
        List<FlowEventResult> events = connector.getEventsForHeightRange("flow.AccountCreated", 0, 30);
        assertNotNull(events, "Events should not be null");
        assertFalse(events.isEmpty(), "Expected account created events but found none.");
        assertEquals(3, events.size(), "Expected 3 account created events.");
    }

    @Test
    void testGetEventsForBlockIds() throws ExecutionException, InterruptedException, TimeoutException {
        FlowBlockHeader latestBlock;

        // Get the latest block header from the access API
        FlowAccessApi.AccessApiCallResponse<FlowBlockHeader> latestBlockResponse = accessAPI.getLatestBlockHeader(true);

        // Handle the response
        if (latestBlockResponse instanceof FlowAccessApi.AccessApiCallResponse.Success) {
            latestBlock = ((FlowAccessApi.AccessApiCallResponse.Success<FlowBlockHeader>) latestBlockResponse).getData();
        } else if (latestBlockResponse instanceof FlowAccessApi.AccessApiCallResponse.Error errorResponse) {
            throw new RuntimeException(errorResponse.getMessage(), errorResponse.getThrowable());
        } else {
            throw new RuntimeException("Unknown response type");
        }

        List<FlowId> blockIds = List.of(latestBlock.getId());
        List<FlowEventResult> events = connector.getEventsForBlockIds("flow.AccountCreated", blockIds);

        assertNotNull(events, "Events should not be null");
        assertFalse(events.isEmpty(), "Expected events for the provided block IDs but found none.");
        assertEquals(1, events.size(), "Expected 1 account created event.");
    }

    @Test
    void testAccountCreatedEvents() throws ExecutionException, InterruptedException, TimeoutException {
        List<FlowEventResult> events = connector.getAccountCreatedEvents(0, 30);
        assertNotNull(events, "Events should not be null");
        assertFalse(events.isEmpty(), "Expected account created events but found none.");
        for (FlowEventResult block : events) {
            for (FlowEvent event : block.getEvents()) {
                assertEquals("flow.AccountCreated", event.getType());
            }
        }
    }

    @Test
    void testTransactionResultEvents() throws ExecutionException, InterruptedException, TimeoutException {
        FlowTransactionResult txResult = connector.getTransactionResult(txID);
        assertNotNull(txResult, "Transaction result should not be null");
        assertFalse(txResult.getEvents().isEmpty(), "Expected events in transaction result but found none.");

        Set<String> expectedEventTypes = Set.of("Withdrawn", "TokensDeposited", "Deposited", "AccountCreated", "AccountKeyAdded", "TokensWithdrawn", "StorageCapabilityControllerIssued", "CapabilityPublished");

        for (FlowEvent event : txResult.getEvents()) {
            String eventType = event.getType().split("\\.")[event.getType().split("\\.").length - 1];
            assertTrue(expectedEventTypes.contains(eventType), "Unexpected event type: " + eventType);
        }
    }
}
