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
        val testScope = CoroutineScope(Dispatchers.IO + Job())
        val receivedEvents = mutableListOf<FlowEvent>()
        try {
            val streamJob = launch {
                withTimeoutOrNull(10_000L) {
                    subscribeEventsExample.streamEvents(testScope, receivedEvents)
                }
            }

            // Trigger a sample transaction
            val publicKey = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256).public
            accessAPIConnector.sendSampleTransaction(
                serviceAccount.flowAddress,
                publicKey
            )

            delay(3000L)
            testScope.cancel()
            streamJob.join()
        } catch (e: CancellationException) {
            println("Test scope cancelled: ${e.message}")
        }

        // Validate that events have been received and processed
        assertTrue(receivedEvents.isNotEmpty(), "Should have received at least one event")
        receivedEvents.forEach { event ->
            assertNotNull(event.type, "Event type should not be null")
            assertNotNull(event.transactionId, "Transaction ID should not be null")
        }
    }
}
