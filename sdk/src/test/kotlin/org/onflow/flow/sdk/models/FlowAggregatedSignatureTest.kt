package org.onflow.flow.sdk.models

import org.onflow.flow.sdk.FlowAggregatedSignature
import org.onflow.flow.sdk.FlowId
import org.onflow.flow.sdk.FlowSignature
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.onflow.protobuf.entities.BlockSealOuterClass

class FlowAggregatedSignatureTest {
    @Test
    fun `test FlowAggregatedSignature equals and hashCode`() {
        val signature1 = FlowSignature("signature1".toByteArray())
        val signature2 = FlowSignature("signature2".toByteArray())

        val signerId1 = FlowId.of("signerId1".toByteArray())
        val signerId2 = FlowId.of("signerId2".toByteArray())

        val aggregatedSignature1 = FlowAggregatedSignature(
            verifierSignatures = listOf(signature1),
            signerIds = listOf(signerId1)
        )

        val aggregatedSignature2 = FlowAggregatedSignature(
            verifierSignatures = listOf(signature1),
            signerIds = listOf(signerId1)
        )

        val aggregatedSignature3 = FlowAggregatedSignature(
            verifierSignatures = listOf(signature2),
            signerIds = listOf(signerId2)
        )

        // Test equality
        assertEquals(aggregatedSignature1, aggregatedSignature2)
        assertNotEquals(aggregatedSignature1, aggregatedSignature3)

        // Test hashCode
        assertEquals(aggregatedSignature1.hashCode(), aggregatedSignature2.hashCode())
        assertNotEquals(aggregatedSignature1.hashCode(), aggregatedSignature3.hashCode())
    }

    @Test
    fun `test FlowAggregatedSignature of function`() {
        // Mock BlockSealOuterClass.AggregatedSignature
        val aggregatedSignatureProto = Mockito.mock(BlockSealOuterClass.AggregatedSignature::class.java)

        val signatureBytes = "signature".toByteArray()
        val signerIdBytes = "signerId".toByteArray()

        Mockito.`when`(aggregatedSignatureProto.verifierSignaturesList).thenReturn(
            listOf(com.google.protobuf.ByteString.copyFrom(signatureBytes))
        )
        Mockito.`when`(aggregatedSignatureProto.signerIdsList).thenReturn(
            listOf(com.google.protobuf.ByteString.copyFrom(signerIdBytes))
        )

        val flowAggregatedSignature = FlowAggregatedSignature.of(aggregatedSignatureProto)

        assertEquals(1, flowAggregatedSignature.verifierSignatures.size)
        assertEquals(FlowSignature(signatureBytes), flowAggregatedSignature.verifierSignatures[0])

        assertEquals(1, flowAggregatedSignature.signerIds.size)
        assertEquals(FlowId.of(signerIdBytes), flowAggregatedSignature.signerIds[0])
    }

    @Test
    fun `test FlowAggregatedSignature builder function`() {
        val signature1 = FlowSignature("signature1".toByteArray())
        val signerId1 = FlowId.of("signerId1".toByteArray())

        val aggregatedSignature = FlowAggregatedSignature(
            verifierSignatures = listOf(signature1),
            signerIds = listOf(signerId1)
        )

        val builderResult = aggregatedSignature.builder()

        assertEquals(listOf(signature1.byteStringValue), builderResult.verifierSignaturesList)
        assertEquals(listOf(signerId1.byteStringValue), builderResult.signerIdsList)
    }
}
