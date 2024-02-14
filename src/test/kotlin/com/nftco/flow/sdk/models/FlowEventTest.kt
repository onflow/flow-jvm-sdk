package com.nftco.flow.sdk.models

import com.google.protobuf.ByteString
import com.nftco.flow.sdk.FlowEvent
import com.nftco.flow.sdk.FlowEventPayload
import com.nftco.flow.sdk.FlowId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.onflow.protobuf.entities.EventOuterClass

class FlowEventTest {
    @Test
    fun `Test building FlowEvent from EventOuterClass`() {
        val type = "example_event"
        val transactionIdBytes = byteArrayOf(1, 2, 3)
        val transactionIndex = 0
        val eventIndex = 1
        val payloadBytes = byteArrayOf(4, 5, 6, 7)

        val eventBuilder = EventOuterClass.Event.newBuilder()
            .setType(type)
            .setTransactionId(ByteString.copyFrom(transactionIdBytes))
            .setTransactionIndex(transactionIndex)
            .setEventIndex(eventIndex)
            .setPayload(ByteString.copyFrom(payloadBytes))

        val flowEvent = FlowEvent.of(eventBuilder.build())

        assertEquals(type, flowEvent.type)
        assertEquals(FlowId.of(transactionIdBytes), flowEvent.transactionId)
        assertEquals(transactionIndex, flowEvent.transactionIndex)
        assertEquals(eventIndex, flowEvent.eventIndex)
        assertEquals(FlowEventPayload(payloadBytes), flowEvent.payload)
    }

    @Test
    fun `Test building EventOuterClass from FlowEvent`() {
        val type = "example_event"
        val transactionId = FlowId.of(byteArrayOf(1, 2, 3))
        val transactionIndex = 0
        val eventIndex = 1
        val payload = FlowEventPayload(byteArrayOf(4, 5, 6, 7))

        val flowEvent = FlowEvent(type, transactionId, transactionIndex, eventIndex, payload)

        val eventBuilder = flowEvent.builder()
        val event = eventBuilder.build()

        assertEquals(type, event.type)
        assertEquals(ByteString.copyFrom(transactionId.bytes), event.transactionId)
        assertEquals(transactionIndex, event.transactionIndex)
        assertEquals(eventIndex, event.eventIndex)
        assertEquals(ByteString.copyFrom(payload.bytes), event.payload)
    }
}
