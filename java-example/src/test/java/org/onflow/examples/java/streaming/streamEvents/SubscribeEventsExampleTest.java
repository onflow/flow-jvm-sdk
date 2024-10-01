package org.onflow.examples.java.streaming.streamEvents;

import kotlinx.coroutines.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onflow.examples.java.AccessAPIConnector;
import org.onflow.flow.common.test.FlowEmulatorProjectTest;
import org.onflow.flow.common.test.FlowServiceAccountCredentials;
import org.onflow.flow.common.test.FlowTestClient;
import org.onflow.flow.common.test.TestAccount;
import org.onflow.flow.sdk.*;
import org.onflow.flow.sdk.crypto.Crypto;
import org.onflow.flow.sdk.crypto.PrivateKey;
import org.onflow.flow.sdk.crypto.PublicKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import static org.junit.jupiter.api.Assertions.*;

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
public class SubscribeEventsExampleTest {

    @FlowServiceAccountCredentials
    private TestAccount serviceAccount;

    @FlowTestClient
    private FlowAccessApi accessAPI;

    private SubscribeEventsExample subscribeEventsExample;
    private AccessAPIConnector accessAPIConnector;

    @BeforeEach
    public void setup() {
        PrivateKey privateKey = serviceAccount.getPrivateKey();
        subscribeEventsExample = new SubscribeEventsExample(privateKey, accessAPI);
        accessAPIConnector = new AccessAPIConnector(privateKey, accessAPI);
    }

    @Test
    public void canStreamAndReceiveBlockEvents() throws Exception {
        // Create a CoroutineScope with IO Dispatcher and a Job
        CoroutineScope testScope = new CoroutineScope(Dispatchers.getIO().plus(new Job()));

        // Create a list to hold received events
        List<FlowEvent> receivedEvents = new ArrayList<>();

        try {
            // Launch a coroutine to stream events
            Job streamJob = BuildersKt.launch(testScope, Dispatchers.getIO(), CoroutineStart.DEFAULT, (scope, continuation) -> {
                // Set a timeout to ensure the test completes within 10 seconds
                BuildersKt.withTimeoutOrNull(testScope.getCoroutineContext(), 10_000L, continuation2 -> {
                    subscribeEventsExample.streamEvents(receivedEvents);
                    return null;
                });
                return null;
            });

            // Trigger a sample transaction
            PublicKey publicKey = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256).getPublic();
            accessAPIConnector.sendSampleTransaction(serviceAccount.getFlowAddress(), publicKey);

            // Wait for 3 seconds to allow events to be received
            Thread.sleep(3000);

            // Cancel the test scope
            testScope.cancel();

            // Wait for the stream job to complete
            streamJob.join(null);
        } catch (CancellationException e) {
            System.out.println("Test scope cancelled: " + e.getMessage());
        }

        // Validate that events have been received and processed
        assertFalse(receivedEvents.isEmpty(), "Should have received at least one event");
        for (FlowEvent event : receivedEvents) {
            assertNotNull(event.getType(), "Event type should not be null");
            assertNotNull(event.getTransactionId(), "Transaction ID should not be null");
        }
    }
}
