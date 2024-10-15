package org.onflow.flow.sdk.models

import com.google.protobuf.ByteString
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.onflow.flow.sdk.FlowExecutionReceiptMeta
import org.onflow.flow.sdk.FlowId
import org.onflow.flow.sdk.FlowSignature
import org.onflow.protobuf.entities.ExecutionResultOuterClass

class FlowExecutionReceiptMetaTest {
    @Test
    fun `test of() method`() {
        val executorIdBytes = ByteArray(32) { 0x01 }
        val resultIdBytes = ByteArray(32) { 0x02 }
        val spocksBytes = listOf(ByteArray(32) { 0x03 }, ByteArray(32) { 0x04 })
        val executorSignatureBytes = ByteArray(32) { 0x05 }

        val grpcMeta = mock(ExecutionResultOuterClass.ExecutionReceiptMeta::class.java)
        `when`(grpcMeta.executorId).thenReturn(ByteString.copyFrom(executorIdBytes))
        `when`(grpcMeta.resultId).thenReturn(ByteString.copyFrom(resultIdBytes))
        `when`(grpcMeta.spocksList).thenReturn(spocksBytes.map { ByteString.copyFrom(it) })
        `when`(grpcMeta.executorSignature).thenReturn(ByteString.copyFrom(executorSignatureBytes))

        val flowMeta = FlowExecutionReceiptMeta.of(grpcMeta)

        assertEquals(FlowId.of(executorIdBytes), flowMeta.executorId)
        assertEquals(FlowId.of(resultIdBytes), flowMeta.resultId)
        assertArrayEquals(spocksBytes[0], flowMeta.spocks[0])
        assertArrayEquals(spocksBytes[1], flowMeta.spocks[1])
        assertEquals(FlowSignature(executorSignatureBytes), flowMeta.executorSignature)
    }

    @Test
    fun `test builder() method`() {
        val executorId = FlowId.of(ByteArray(32) { 0x01 })
        val resultId = FlowId.of(ByteArray(32) { 0x02 })
        val spocks = listOf(ByteArray(32) { 0x03 }, ByteArray(32) { 0x04 })
        val executorSignature = FlowSignature(ByteArray(32) { 0x05 })

        val flowMeta = FlowExecutionReceiptMeta(executorId, resultId, spocks, executorSignature)
        val result = flowMeta.builder().build()

        assertEquals(executorId.byteStringValue, result.executorId)
        assertEquals(resultId.byteStringValue, result.resultId)
        assertEquals(spocks.size, result.spocksCount)
        assertEquals(ByteString.copyFrom(spocks[0]), result.spocksList[0])
        assertEquals(ByteString.copyFrom(spocks[1]), result.spocksList[1])
        assertEquals(executorSignature.byteStringValue, result.executorSignature)
    }

    @Test
    fun `test equals() method`() {
        val executorId1 = FlowId.of(ByteArray(32) { 0x01 })
        val resultId1 = FlowId.of(ByteArray(32) { 0x02 })
        val spocks1 = listOf(ByteArray(32) { 0x03 })
        val executorSignature1 = FlowSignature(ByteArray(32) { 0x04 })

        val executorId2 = FlowId.of(ByteArray(32) { 0x05 })
        val resultId2 = FlowId.of(ByteArray(32) { 0x06 })
        val spocks2 = listOf(ByteArray(32) { 0x07 })
        val executorSignature2 = FlowSignature(ByteArray(32) { 0x08 })

        val meta1 = FlowExecutionReceiptMeta(executorId1, resultId1, spocks1, executorSignature1)
        val meta2 = FlowExecutionReceiptMeta(executorId1, resultId1, spocks1, executorSignature1)
        val meta3 = FlowExecutionReceiptMeta(executorId2, resultId2, spocks2, executorSignature2)

        assertEquals(meta1, meta2)
        assertNotEquals(meta1, meta3)
    }

    @Test
    fun `test hashCode() method`() {
        val executorId1 = FlowId.of(ByteArray(32) { 0x01 })
        val resultId1 = FlowId.of(ByteArray(32) { 0x02 })
        val spocks1 = listOf(ByteArray(32) { 0x03 })
        val executorSignature1 = FlowSignature(ByteArray(32) { 0x04 })

        val executorId2 = FlowId.of(ByteArray(32) { 0x05 })
        val resultId2 = FlowId.of(ByteArray(32) { 0x06 })
        val spocks2 = listOf(ByteArray(32) { 0x07 })
        val executorSignature2 = FlowSignature(ByteArray(32) { 0x08 })

        val meta1 = FlowExecutionReceiptMeta(executorId1, resultId1, spocks1, executorSignature1)
        val meta2 = FlowExecutionReceiptMeta(executorId1, resultId1, spocks1, executorSignature1)
        val meta3 = FlowExecutionReceiptMeta(executorId2, resultId2, spocks2, executorSignature2)

        assertEquals(meta1.hashCode(), meta2.hashCode())
        assertNotEquals(meta1.hashCode(), meta3.hashCode())
    }
}
