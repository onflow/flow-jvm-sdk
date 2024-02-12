package com.nftco.flow.sdk.models

import com.nftco.flow.sdk.FlowAddress
import com.nftco.flow.sdk.FlowSignature
import com.nftco.flow.sdk.FlowTransactionSignature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.onflow.protobuf.entities.TransactionOuterClass

class FlowTransactionSignatureTest {

    @Test
    fun `Test creation from TransactionOuterClass Transaction Signature`() {
        val address = FlowAddress("0x123456")
        val keyIndex = 1
        val signature = FlowSignature(byteArrayOf(1, 2, 3, 4))

        val signatureProtoBuilder = TransactionOuterClass.Transaction.Signature.newBuilder()
            .setAddress(address.byteStringValue)
            .setKeyId(keyIndex)
            .setSignature(signature.byteStringValue)

        val flowTransactionSignature = FlowTransactionSignature.of(signatureProtoBuilder.build())

        assertEquals(address, flowTransactionSignature.address)
        assertEquals(keyIndex, flowTransactionSignature.signerIndex)
        assertEquals(keyIndex, flowTransactionSignature.keyIndex)
        assertEquals(signature, flowTransactionSignature.signature)
    }

    @Test
    fun `Test builder`() {
        val address = FlowAddress("0x123456")
        val keyIndex = 1
        val signature = FlowSignature(byteArrayOf(1, 2, 3, 4))

        val expectedProtoBuilder = TransactionOuterClass.Transaction.Signature.newBuilder()
            .setAddress(address.byteStringValue)
            .setKeyId(keyIndex)
            .setSignature(signature.byteStringValue)

        val actualProtoBuilder = FlowTransactionSignature(address, keyIndex, keyIndex, signature).builder()

        assertEquals(expectedProtoBuilder.build(), actualProtoBuilder.build())
    }
}
