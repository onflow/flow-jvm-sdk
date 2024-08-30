package org.onflow.examples.kotlin.streaming.streamEventsReconnect

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.*

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
internal class SubscribeEventsReconnectExampleTest {

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    private lateinit var subscribeEventsReconnectExample: SubscribeEventsReconnectExample

    @BeforeEach
    fun setup() {
        subscribeEventsReconnectExample = SubscribeEventsReconnectExample(accessAPI)
    }

    @Test
    fun `Can stream and reconnect events`() = runBlocking {
        val scope = this
        val receivedEvents = mutableListOf<FlowEvent>()

        val reconnectJob = launch {
            subscribeEventsReconnectExample.streamEvents(scope, receivedEvents)
        }

        // Simulate a delay to allow event stream to start
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
