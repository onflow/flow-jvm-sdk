package com.nftco.flow.sdk.models

import com.nftco.flow.sdk.FlowSignature
import com.nftco.flow.sdk.hexToBytes
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
