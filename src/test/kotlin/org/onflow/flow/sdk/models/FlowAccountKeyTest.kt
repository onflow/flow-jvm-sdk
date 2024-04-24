package org.onflow.flow.sdk.models

import com.google.protobuf.ByteString
import org.onflow.flow.sdk.FlowAccountKey
import org.onflow.flow.sdk.FlowPublicKey
import org.onflow.flow.sdk.HashAlgorithm
import org.onflow.flow.sdk.SignatureAlgorithm
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.onflow.protobuf.entities.AccountOuterClass

class FlowAccountKeyTest {
    @Test
    fun `Test creating FlowAccountKey from protobuf`() {
        val protobufKey = AccountOuterClass.AccountKey.newBuilder()
            .setIndex(1)
            .setPublicKey(ByteString.copyFromUtf8("0x1234"))
            .setSignAlgo(SignatureAlgorithm.ECDSA_P256.code)
            .setHashAlgo(HashAlgorithm.SHA2_256.code)
            .setWeight(100)
            .setSequenceNumber(10)
            .setRevoked(false)
            .build()

        val flowAccountKey = FlowAccountKey.of(protobufKey)

        assertEquals(1, flowAccountKey.id)
        assertEquals("0x1234", flowAccountKey.publicKey.stringValue)
        assertEquals(SignatureAlgorithm.ECDSA_P256, flowAccountKey.signAlgo)
        assertEquals(HashAlgorithm.SHA2_256, flowAccountKey.hashAlgo)
        assertEquals(100, flowAccountKey.weight)
        assertEquals(10, flowAccountKey.sequenceNumber)
        assertFalse(flowAccountKey.revoked)
    }

    @Test
    fun `Test creating protobuf Builder from FlowAccountKey`() {
        val flowAccountKey = FlowAccountKey(
            id = 2,
            publicKey = FlowPublicKey("0x5678".toByteArray()),
            signAlgo = SignatureAlgorithm.ECDSA_P256,
            hashAlgo = HashAlgorithm.SHA2_256,
            weight = 200,
            sequenceNumber = 20,
            revoked = true
        )

        val protobufBuilder = flowAccountKey.builder()

        assertEquals(2, protobufBuilder.index)
        assertEquals("0x5678", protobufBuilder.publicKey.toStringUtf8())
        assertEquals(SignatureAlgorithm.ECDSA_P256.code, protobufBuilder.signAlgo)
        assertEquals(HashAlgorithm.SHA2_256.code, protobufBuilder.hashAlgo)
        assertEquals(200, protobufBuilder.weight)
        assertEquals(20, protobufBuilder.sequenceNumber)
        assertTrue(protobufBuilder.revoked)
    }

    @Test
    fun `Test getting encoded value`() {
        val flowAccountKey = FlowAccountKey(
            id = 3,
            publicKey = FlowPublicKey("0x9abc".toByteArray()),
            signAlgo = SignatureAlgorithm.ECDSA_P256,
            hashAlgo = HashAlgorithm.SHA2_256,
            weight = 300,
            sequenceNumber = 30,
            revoked = false
        )

        val encodedValue = flowAccountKey.encoded

        assertEquals(13, encodedValue.size)
    }
}
