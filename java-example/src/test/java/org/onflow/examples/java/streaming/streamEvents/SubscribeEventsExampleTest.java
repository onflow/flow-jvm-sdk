package org.onflow.examples.java.streaming.streamEvents;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
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
        // Create a Job and CoroutineContext with IO Dispatcher
        Job job = JobKt.Job(null); // Create a standalone Job
        CoroutineContext context = Dispatchers.getIO().plus(job); // Combine Dispatcher and Job into CoroutineContext

        // Create a CoroutineScope using the CoroutineScopeKt helper
        CoroutineScope testScope = CoroutineScopeKt.CoroutineScope(context);


        List<FlowEvent> receivedEvents = new ArrayList<>();

        try {
            // Launch a coroutine to stream events
            Job streamJob = BuildersKt.launch(testScope, Dispatchers.getIO(), CoroutineStart.DEFAULT, (scope, continuation) -> {
                try {
                    // Stream events directly
                    subscribeEventsExample.streamEvents(receivedEvents);
                    // Resume the coroutine on success
                    continuation.resumeWith(Unit.INSTANCE);
                } catch (Exception e) {
                    // Resume the coroutine with exception in case of failure
                    continuation.resumeWith(e);
                }
                return Unit.INSTANCE;
            });


            // Trigger a sample transaction
            PublicKey publicKey = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256).getPublic();
            accessAPIConnector.sendSampleTransaction(serviceAccount.getFlowAddress(), publicKey);

            Thread.sleep(3000);
            job.cancel(null);
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
