package org.onflow.flow.sdk.models

import org.onflow.flow.sdk.FlowEventPayload
import org.onflow.flow.sdk.cadence.StringField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FlowEventPayloadTest {
    @Test
    fun `Test initialization from JSON Cadence`() {
        val jsonCadence = StringField("test")

        val flowEventPayload = FlowEventPayload(jsonCadence)
        assertEquals(jsonCadence.decodeToAny(), flowEventPayload.jsonCadence.decodeToAny())
    }

    @Test
    fun `Test equals`() {
        val bytes = byteArrayOf(1, 2, 3)
        val flowEventPayload1 = FlowEventPayload(bytes)
        val flowEventPayload2 = FlowEventPayload(bytes)

        assertEquals(flowEventPayload1, flowEventPayload2)
    }

    @Test
    fun `Test hashCode`() {
        val bytes = byteArrayOf(1, 2, 3)
        val flowEventPayload = FlowEventPayload(bytes)

        assertEquals(bytes.contentHashCode(), flowEventPayload.hashCode())
    }
}
