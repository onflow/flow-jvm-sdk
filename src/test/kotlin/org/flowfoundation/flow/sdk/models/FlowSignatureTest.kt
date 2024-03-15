package org.flowfoundation.flow.sdk.models

import org.flowfoundation.flow.sdk.FlowSignature
import org.flowfoundation.flow.sdk.hexToBytes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FlowSignatureTest {
    @Test
    fun `Test initialization from hex string`() {
        val hexString = "abcdef1234567890"
        val flowSignature = FlowSignature(hexString)

        assertEquals(hexString.hexToBytes().contentToString(), flowSignature.bytes.contentToString())
    }

    @Test
    fun `Test equals`() {
        val bytes = byteArrayOf(1, 2, 3)
        val flowSignature1 = FlowSignature(bytes)
        val flowSignature2 = FlowSignature(bytes)

        assertEquals(flowSignature1, flowSignature2)
    }

    @Test
    fun `Test hashCode`() {
        val bytes = byteArrayOf(1, 2, 3)
        val flowSignature = FlowSignature(bytes)

        assertEquals(bytes.contentHashCode(), flowSignature.hashCode())
    }
}
