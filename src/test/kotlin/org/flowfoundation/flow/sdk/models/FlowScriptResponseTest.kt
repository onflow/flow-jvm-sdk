package org.flowfoundation.flow.sdk.models

import org.flowfoundation.flow.sdk.FlowScriptResponse
import org.flowfoundation.flow.sdk.cadence.StringField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FlowScriptResponseTest {
    @Test
    fun `Test initialization from JSON Cadence`() {
        val jsonCadence = StringField("test")
        val flowScriptResponse = FlowScriptResponse(jsonCadence)

        assertEquals(jsonCadence.decodeToAny(), flowScriptResponse.jsonCadence.value)
    }

    @Test
    fun `Test equals`() {
        val bytes = byteArrayOf(1, 2, 3)
        val flowScriptResponse1 = FlowScriptResponse(bytes)
        val flowScriptResponse2 = FlowScriptResponse(bytes)

        assertEquals(flowScriptResponse1, flowScriptResponse2)
    }

    @Test
    fun `Test hashCode`() {
        val bytes = byteArrayOf(1, 2, 3)
        val flowScriptResponse = FlowScriptResponse(bytes)

        assertEquals(bytes.contentHashCode(), flowScriptResponse.hashCode())
    }
}
