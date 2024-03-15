package org.flowfoundation.flow.sdk.models

import org.flowfoundation.flow.sdk.FlowCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FlowCodeTest {
    @Test
    fun `Test equality`() {
        val bytes1 = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7)
        val bytes2 = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7)
        val bytes3 = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)

        val flowCode1 = FlowCode(bytes1)
        val flowCode2 = FlowCode(bytes2)
        val flowCode3 = FlowCode(bytes3)

        assertEquals(flowCode1, flowCode2)
        assertEquals(flowCode2, flowCode1)

        assertEquals(false, flowCode1 == flowCode3)
        assertEquals(false, flowCode3 == flowCode1)
    }

    @Test
    fun `Test hashCode`() {
        val bytes = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7)
        val flowCode = FlowCode(bytes)

        val expectedHashCode = bytes.contentHashCode()

        assertEquals(expectedHashCode, flowCode.hashCode())
    }
}
