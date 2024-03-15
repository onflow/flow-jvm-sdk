package org.flowfoundation.flow.sdk.models

import org.flowfoundation.flow.sdk.Flow
import org.flowfoundation.flow.sdk.FlowArgument
import org.flowfoundation.flow.sdk.cadence.Field
import org.flowfoundation.flow.sdk.cadence.StringField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class FlowArgumentTest {
    @Test
    fun `Test initialization from byte array`() {
        val bytes = byteArrayOf(0x01, 0x02, 0x03)
        val flowArgument = FlowArgument(bytes)
        assertEquals(bytes, flowArgument.bytes)
    }

    @Test
    fun `Test initialization from JSON Cadence`() {
        val jsonCadence = StringField("test")
        val flowArgument = FlowArgument(jsonCadence)

        val decodedCadence: Field<Any> = Flow.decodeJsonCadence(flowArgument.bytes)

        assertEquals(jsonCadence, decodedCadence)
        assertEquals(jsonCadence.decodeToAny(), decodedCadence.decodeToAny())
    }

    @Test
    fun `Test equals`() {
        val bytes1 = byteArrayOf(0x01, 0x02, 0x03)
        val bytes2 = byteArrayOf(0x04, 0x05, 0x06)

        val flowArgument1 = FlowArgument(bytes1)
        val flowArgument2 = FlowArgument(bytes1)
        val flowArgument3 = FlowArgument(bytes2)

        assertEquals(flowArgument1, flowArgument2)
        assertNotEquals(flowArgument1, flowArgument3)
    }

    @Test
    fun `Test hashCode`() {
        val bytes1 = byteArrayOf(0x01, 0x02, 0x03)
        val bytes2 = byteArrayOf(0x04, 0x05, 0x06)

        val flowArgument1 = FlowArgument(bytes1)
        val flowArgument2 = FlowArgument(bytes1)
        val flowArgument3 = FlowArgument(bytes2)

        assertEquals(flowArgument1.hashCode(), flowArgument2.hashCode())
        assertNotEquals(flowArgument1.hashCode(), flowArgument3.hashCode())
    }
}
