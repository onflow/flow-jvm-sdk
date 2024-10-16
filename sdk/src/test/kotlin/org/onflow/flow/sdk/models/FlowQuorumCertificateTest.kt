package org.onflow.flow.sdk.models

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import com.google.protobuf.ByteString
import org.onflow.flow.sdk.FlowId
import org.onflow.flow.sdk.FlowQuorumCertificate
import org.onflow.protobuf.entities.BlockHeaderOuterClass

class FlowQuorumCertificateTest {
    @Test
    fun `test of() method`() {
        val view = 123L
        val blockIdBytes = ByteArray(32) { 0x01 }
        val signerIndicesBytes = ByteArray(32) { 0x02 }
        val sigDataBytes = ByteArray(32) { 0x03 }

        val grpcQuorumCertificate = mock(BlockHeaderOuterClass.QuorumCertificate::class.java)
        `when`(grpcQuorumCertificate.view).thenReturn(view)
        `when`(grpcQuorumCertificate.blockId).thenReturn(ByteString.copyFrom(blockIdBytes))
        `when`(grpcQuorumCertificate.signerIndices).thenReturn(ByteString.copyFrom(signerIndicesBytes))
        `when`(grpcQuorumCertificate.sigData).thenReturn(ByteString.copyFrom(sigDataBytes))

        val flowQuorumCertificate = FlowQuorumCertificate.of(grpcQuorumCertificate)

        assertEquals(view, flowQuorumCertificate.view)
        assertArrayEquals(blockIdBytes, flowQuorumCertificate.blockId.bytes)
        assertArrayEquals(signerIndicesBytes, flowQuorumCertificate.signerIndices)
        assertArrayEquals(sigDataBytes, flowQuorumCertificate.sigData)
    }

    @Test
    fun `test builder() method`() {
        val view = 123L
        val blockId = FlowId.of(ByteArray(32) { 0x01 })
        val signerIndices = ByteArray(32) { 0x02 }
        val sigData = ByteArray(32) { 0x03 }

        val flowQuorumCertificate = FlowQuorumCertificate(view, blockId, signerIndices, sigData)

        val builder = flowQuorumCertificate.builder().build()

        assertEquals(view, builder.view)
        assertArrayEquals(blockId.bytes, builder.blockId.toByteArray())
        assertArrayEquals(signerIndices, builder.signerIndices.toByteArray())
        assertArrayEquals(sigData, builder.sigData.toByteArray())
    }

    @Test
    fun `test equals() method`() {
        val view1 = 123L
        val blockId1 = FlowId.of(ByteArray(32) { 0x01 })
        val signerIndices1 = ByteArray(32) { 0x02 }
        val sigData1 = ByteArray(32) { 0x03 }

        val view2 = 124L
        val blockId2 = FlowId.of(ByteArray(32) { 0x04 })
        val signerIndices2 = ByteArray(32) { 0x05 }
        val sigData2 = ByteArray(32) { 0x06 }

        val flowQuorumCertificate1 = FlowQuorumCertificate(view1, blockId1, signerIndices1, sigData1)
        val flowQuorumCertificate2 = FlowQuorumCertificate(view1, blockId1, signerIndices1, sigData1)
        val flowQuorumCertificate3 = FlowQuorumCertificate(view2, blockId2, signerIndices2, sigData2)

        assertEquals(flowQuorumCertificate1, flowQuorumCertificate2)
        assertNotEquals(flowQuorumCertificate1, flowQuorumCertificate3)
    }

    @Test
    fun `test hashCode() method`() {
        val view1 = 123L
        val blockId1 = FlowId.of(ByteArray(32) { 0x01 })
        val signerIndices1 = ByteArray(32) { 0x02 }
        val sigData1 = ByteArray(32) { 0x03 }

        val view2 = 124L
        val blockId2 = FlowId.of(ByteArray(32) { 0x04 })
        val signerIndices2 = ByteArray(32) { 0x05 }
        val sigData2 = ByteArray(32) { 0x06 }

        val flowQuorumCertificate1 = FlowQuorumCertificate(view1, blockId1, signerIndices1, sigData1)
        val flowQuorumCertificate2 = FlowQuorumCertificate(view1, blockId1, signerIndices1, sigData1)
        val flowQuorumCertificate3 = FlowQuorumCertificate(view2, blockId2, signerIndices2, sigData2)

        assertEquals(flowQuorumCertificate1.hashCode(), flowQuorumCertificate2.hashCode())
        assertNotEquals(flowQuorumCertificate1.hashCode(), flowQuorumCertificate3.hashCode())
    }
}
