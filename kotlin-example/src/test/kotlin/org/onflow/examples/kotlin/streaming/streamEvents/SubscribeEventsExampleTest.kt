package org.onflow.examples.kotlin.streaming.streamEvents

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.crypto.Crypto

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class SubscribeEventsExampleTest {
    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var subscribeEventsExample: SubscribeEventsExample
    private lateinit var accessAPIConnector: AccessAPIConnector

    @BeforeEach
    fun setup() {
        val privateKey = serviceAccount.privateKey
        subscribeEventsExample = SubscribeEventsExample(privateKey, accessAPI)
        accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
    }

    @Test
    fun `Can stream and receive block events`() = runBlocking {
        val scope = this
        val receivedEvents = mutableListOf<FlowEvent>()

        // Start streaming events
        val streamJob = launch {
            subscribeEventsExample.streamEvents(scope, receivedEvents)
        }

        delay(3000L)
        // Trigger a sample transaction to generate events
        val publicKey = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256).public
        accessAPIConnector.sendSampleTransaction(
            serviceAccount.flowAddress,
            publicKey
        )
        delay(3000L)
        streamJob.cancelAndJoin()

        // Validate that events have been received and processed
        assertTrue(receivedEvents.isNotEmpty(), "Should have received at least one event")
        receivedEvents.forEach { event ->
            assertNotNull(event.type, "Event type should not be null")
            assertNotNull(event.transactionId, "Transaction ID should not be null")
        }
    }
}
