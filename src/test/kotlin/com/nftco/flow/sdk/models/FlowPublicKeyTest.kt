package com.nftco.flow.sdk.models

import com.nftco.flow.sdk.FlowPublicKey
import com.nftco.flow.sdk.hexToBytes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FlowPublicKeyTest {
    @Test
    fun `Test initialization from hex string`() {
        val hexString = "123456789abcdef0"
        val flowPublicKey = FlowPublicKey(hexString)

        assertEquals(hexString.hexToBytes().contentToString(), flowPublicKey.bytes.contentToString())
    }

    @Test
    fun `Test equals`() {
        val bytes = byteArrayOf(1, 2, 3)
        val flowPublicKey1 = FlowPublicKey(bytes)
        val flowPublicKey2 = FlowPublicKey(bytes)

        assertEquals(flowPublicKey1, flowPublicKey2)
    }

    @Test
    fun `Test hashCode`() {
        val bytes = byteArrayOf(1, 2, 3)
        val flowPublicKey = FlowPublicKey(bytes)

        assertEquals(bytes.contentHashCode(), flowPublicKey.hashCode())
    }
}
