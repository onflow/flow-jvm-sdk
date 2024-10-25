package org.onflow.flow.sdk.models

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.onflow.flow.sdk.FlowAggregatedSignature
import org.onflow.flow.sdk.FlowBlockSeal
import org.onflow.flow.sdk.FlowId
import org.onflow.flow.sdk.FlowSignature
import org.onflow.protobuf.entities.BlockSealOuterClass

class FlowBlockSealTest {
    @Test
    fun `test FlowBlockSeal equals and hashCode`() {
        val blockId = FlowId.of("blockId".toByteArray())
        val executionReceiptId = FlowId.of("executionReceiptId".toByteArray())
        val resultId = FlowId.of("resultId".toByteArray())
        val finalState = "finalState".toByteArray()

        val signature1 = FlowSignature("signature1".toByteArray())
        val signature2 = FlowSignature("signature2".toByteArray())

        val aggregatedSignature1 = FlowAggregatedSignature(
            verifierSignatures = listOf(signature1),
            signerIds = listOf(blockId)
        )

        val seal1 = FlowBlockSeal(
            blockId = blockId,
            executionReceiptId = executionReceiptId,
            executionReceiptSignatures = listOf(signature1),
            resultApprovalSignatures = listOf(signature2),
            finalState = finalState,
            resultId = resultId,
            aggregatedApprovalSigs = listOf(aggregatedSignature1)
        )

        val seal2 = FlowBlockSeal(
            blockId = blockId,
            executionReceiptId = executionReceiptId,
            executionReceiptSignatures = listOf(signature1),
            resultApprovalSignatures = listOf(signature2),
            finalState = finalState,
            resultId = resultId,
            aggregatedApprovalSigs = listOf(aggregatedSignature1)
        )

        // Testing equals
        assertEquals(seal1, seal2)

        // Testing hashCode
        assertEquals(seal1.hashCode(), seal2.hashCode())
    }

    @Test
    fun `test FlowBlockSeal of function`() {
        // Mock BlockSealOuterClass.BlockSeal
        val blockSealProto = Mockito.mock(BlockSealOuterClass.BlockSeal::class.java)

        val blockIdBytes = "blockId".toByteArray()
        val executionReceiptIdBytes = "executionReceiptId".toByteArray()
        val resultIdBytes = "resultId".toByteArray()
        val finalStateBytes = "finalState".toByteArray()

        Mockito.`when`(blockSealProto.blockId).thenReturn(
            com.google.protobuf.ByteString
                .copyFrom(blockIdBytes)
        )
        Mockito.`when`(blockSealProto.executionReceiptId).thenReturn(
            com.google.protobuf.ByteString
                .copyFrom(executionReceiptIdBytes)
        )
        Mockito.`when`(blockSealProto.resultId).thenReturn(
            com.google.protobuf.ByteString
                .copyFrom(resultIdBytes)
        )
        Mockito.`when`(blockSealProto.finalState).thenReturn(
            com.google.protobuf.ByteString
                .copyFrom(finalStateBytes)
        )
        Mockito.`when`(blockSealProto.executionReceiptSignaturesList).thenReturn(
            listOf(
                com.google.protobuf.ByteString
                    .copyFrom("signature1".toByteArray())
            )
        )
        Mockito.`when`(blockSealProto.resultApprovalSignaturesList).thenReturn(
            listOf(
                com.google.protobuf.ByteString
                    .copyFrom("signature2".toByteArray())
            )
        )
        Mockito.`when`(blockSealProto.aggregatedApprovalSigsList).thenReturn(
            listOf(
                BlockSealOuterClass.AggregatedSignature.newBuilder().build()
            )
        )

        val flowBlockSeal = FlowBlockSeal.of(blockSealProto)

        assertEquals(FlowId.of(blockIdBytes), flowBlockSeal.blockId)
        assertEquals(FlowId.of(executionReceiptIdBytes), flowBlockSeal.executionReceiptId)
        assertEquals(FlowId.of(resultIdBytes), flowBlockSeal.resultId)
        assertArrayEquals(finalStateBytes, flowBlockSeal.finalState)
    }

    @Test
    fun `test FlowBlockSeal builder function`() {
        val blockId = FlowId.of("blockId".toByteArray())
        val executionReceiptId = FlowId.of("executionReceiptId".toByteArray())
        val resultId = FlowId.of("resultId".toByteArray())
        val finalState = "finalState".toByteArray()

        val signature1 = FlowSignature("signature1".toByteArray())
        val signature2 = FlowSignature("signature2".toByteArray())
        val aggregatedSignature1 = FlowAggregatedSignature(
            verifierSignatures = listOf(signature1),
            signerIds = listOf(blockId)
        )

        val seal = FlowBlockSeal(
            blockId = blockId,
            executionReceiptId = executionReceiptId,
            executionReceiptSignatures = listOf(signature1),
            resultApprovalSignatures = listOf(signature2),
            finalState = finalState,
            resultId = resultId,
            aggregatedApprovalSigs = listOf(aggregatedSignature1)
        )

        val builderResult = seal.builder()

        assertEquals(blockId.byteStringValue, builderResult.blockId)
        assertEquals(executionReceiptId.byteStringValue, builderResult.executionReceiptId)
        assertEquals(resultId.byteStringValue, builderResult.resultId)
        assertEquals(finalState.toList(), builderResult.finalState.toByteArray().toList())
        assertEquals(listOf(signature1.byteStringValue), builderResult.executionReceiptSignaturesList)
        assertEquals(listOf(signature2.byteStringValue), builderResult.resultApprovalSignaturesList)
    }
}
