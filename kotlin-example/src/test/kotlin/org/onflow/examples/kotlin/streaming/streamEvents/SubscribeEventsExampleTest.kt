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
    fun `Can subscribe to latest block events`() = runBlocking {
        val scope = this
        val (eventChannel, errorChannel) = subscribeEventsExample.subscribeToLatestBlockEvents(scope)

        // Send a sample transaction to trigger events
        val publicKey = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256).public
        accessAPIConnector.sendSampleTransaction(
            serviceAccount.flowAddress,
            publicKey
        )

        val receivedEvents = mutableListOf<FlowEvent>()

        // Collect events and handle errors
        val job = launch {
            try {
                for (events in eventChannel) {
                    receivedEvents.addAll(events)
                }
            } catch (e: Exception) {
                println("Error receiving events: ${e.message}")
                e.printStackTrace()
            } finally {
                eventChannel.cancel()
                errorChannel.cancel()
            }
        }

        // Wait for events to be received
        delay(5000L)
        job.cancelAndJoin()

        // Verify that events were received
        assertTrue(receivedEvents.isNotEmpty(), "Should have received at least one event")

        receivedEvents.forEach { event ->
            assertNotNull(event.type, "Event type should not be null")
            assertNotNull(event.transactionId, "Transaction ID should not be null")
        }
    }
}
