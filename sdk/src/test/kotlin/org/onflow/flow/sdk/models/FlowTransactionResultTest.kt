package org.onflow.flow.sdk.models

import com.google.protobuf.ByteString
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.CompositeAttribute
import org.onflow.flow.sdk.cadence.CompositeValue
import org.onflow.flow.sdk.cadence.EventField
import org.onflow.flow.sdk.cadence.StringField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.entities.TransactionOuterClass

class FlowTransactionResultTest {
    @Test
    fun `Test initialization from Transaction Result Response`() {
        val status = FlowTransactionStatus.EXECUTED
        val statusCode = 0
        val errorMessage = ""
        val events = listOf(
            FlowEvent("type1", FlowId("0x1234"), 0, 0, FlowEventPayload(StringField("payload1"))),
            FlowEvent("type2", FlowId("0x2234"), 0, 0, FlowEventPayload(StringField("payload2")))
        )

        val responseBuilder = Access.TransactionResultResponse.newBuilder()
            .setStatus(TransactionOuterClass.TransactionStatus.EXECUTED)
            .setStatusCode(statusCode)
            .setErrorMessage(errorMessage)
            .setBlockId(ByteString.copyFromUtf8("blockId"))
            .setBlockHeight(1L)
            .setTransactionId(ByteString.copyFromUtf8("transactionId"))
            .setCollectionId(ByteString.copyFromUtf8("collectionId"))
            .setComputationUsage(1L)
            .addAllEvents(events.map { it.builder().build() })


        val flowTransactionResult = FlowTransactionResult.of(responseBuilder.build())

        assertEquals(status, flowTransactionResult.status)
        assertEquals(statusCode, flowTransactionResult.statusCode)
        assertEquals(errorMessage, flowTransactionResult.errorMessage)
        assertEquals(events, flowTransactionResult.events)
    }

    @Test
    fun `Test throwOnError with invalid status code`() {
        val status = FlowTransactionStatus.of(5)
        val invalidStatusCode = 1
        val errorMessage = "Error message"

        val flowId = FlowId("0x01")

        val flowTransactionResult = FlowTransactionResult(
            status,
            invalidStatusCode,
            errorMessage,
            emptyList(),
            flowId,
            1L,
            flowId,
            flowId,
            1L
        )

        assertThrows<FlowException> { flowTransactionResult.throwOnError() }
    }

    @Test
    fun `Test getEventsOfType`() {
        val eventField1 = EventField(CompositeValue("payload1", arrayOf(CompositeAttribute("1", StringField("payload1")))))
        val eventField2 = EventField(CompositeValue("payload2", arrayOf(CompositeAttribute("2", StringField("payload2")))))
        val eventField3 = EventField(CompositeValue("payload3", arrayOf(CompositeAttribute("3", StringField("payload3")))))

        val event1 = FlowEvent("type1", FlowId("0x1234"), 0, 0, FlowEventPayload(eventField1))
        val event2 = FlowEvent("type2", FlowId("0x2234"), 0, 0, FlowEventPayload(eventField2))
        val event3 = FlowEvent("sub-type1", FlowId("0x3234"), 0, 0, FlowEventPayload(eventField3))

        val flowId = FlowId("0x01")

        val flowTransactionResult = FlowTransactionResult(
            FlowTransactionStatus.SEALED,
            0,
            "",
            listOf(event1, event2, event3),
            flowId,
            1L,
            flowId,
            flowId,
            1L
        )

        // Events of a specific type
        val eventsOfType1 = flowTransactionResult.getEventsOfType("type1")
        assertEquals(2, eventsOfType1.size)
        assertEquals(listOf(eventField1, eventField3), eventsOfType1)

        // Events of a specific type with exact match
        val eventsOfType1Exact = flowTransactionResult.getEventsOfType("type1", exact = true)
        assertEquals(1, eventsOfType1Exact.size)
        assertEquals(listOf(eventField1), eventsOfType1Exact)

        // Events of a non-existing type
        val eventsOfTypeNonExisting = flowTransactionResult.getEventsOfType("nonExistingType")
        assertEquals(0, eventsOfTypeNonExisting.size)
    }
}
