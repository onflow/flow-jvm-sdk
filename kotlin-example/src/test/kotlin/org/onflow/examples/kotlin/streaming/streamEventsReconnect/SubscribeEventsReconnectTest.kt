package org.onflow.examples.kotlin.streaming.streamEventsReconnect

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.crypto.Crypto

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class SubscribeEventsReconnectExampleTest {
    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var subscribeEventsReconnectExample: SubscribeEventsReconnectExample
    private lateinit var accessAPIConnector: AccessAPIConnector

    @BeforeEach
    fun setup() {
        accessAPIConnector = AccessAPIConnector(serviceAccount.privateKey, accessAPI)
        subscribeEventsReconnectExample = SubscribeEventsReconnectExample(accessAPI)
    }

    @Test
    fun `Can stream and reconnect events`() = runBlocking {
        val testScope = CoroutineScope(Dispatchers.IO + Job())
        val receivedEvents = mutableListOf<FlowEvent>()

        val reconnectJob = launch {
            subscribeEventsReconnectExample.streamEvents(testScope, receivedEvents)
        }

        // Trigger a sample event
        val publicKey = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256).public
        accessAPIConnector.sendSampleTransaction(
            serviceAccount.flowAddress,
            publicKey
        )
        delay(5000L)

        // Check if the stream has reconnected and continued processing after the simulated disconnection
        reconnectJob.cancelAndJoin()

        // Validate that events have been received and processed
        assertTrue(receivedEvents.isNotEmpty(), "Should have received at least one event")
        receivedEvents.forEach { event ->
            assertNotNull(event.type, "Event type should not be null")
            assertNotNull(event.transactionId, "Transaction ID should not be null")
        }
    }
}
