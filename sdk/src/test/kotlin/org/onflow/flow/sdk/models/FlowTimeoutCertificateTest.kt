package org.onflow.flow.sdk.models

import com.google.protobuf.ByteString
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.onflow.flow.sdk.FlowQuorumCertificate
import org.onflow.flow.sdk.FlowTimeoutCertificate
import org.onflow.protobuf.entities.BlockHeaderOuterClass

class FlowTimeoutCertificateTest {
    @Test
    fun `test of() method`() {
        val view = 123L
        val highQcViews = listOf(456L, 789L)
        val signerIndicesBytes = ByteArray(32) { 0x01 }
        val sigDataBytes = ByteArray(32) { 0x02 }

        // mock QuorumCertificate
        val grpcHighestQc = mock(BlockHeaderOuterClass.QuorumCertificate::class.java)
        `when`(grpcHighestQc.view).thenReturn(view)
        `when`(grpcHighestQc.blockId).thenReturn(ByteString.copyFrom(ByteArray(32) { 0x01 }))
        `when`(grpcHighestQc.signerIndices).thenReturn(ByteString.copyFrom(ByteArray(32) { 0x02 }))
        `when`(grpcHighestQc.sigData).thenReturn(ByteString.copyFrom(ByteArray(32) { 0x03 }))

        val highestQc = FlowQuorumCertificate.of(grpcHighestQc)

        // mock TimeoutCertificate
        val grpcTimeoutCertificate = mock(BlockHeaderOuterClass.TimeoutCertificate::class.java)
        `when`(grpcTimeoutCertificate.view).thenReturn(view)
        `when`(grpcTimeoutCertificate.highQcViewsList).thenReturn(highQcViews)
        `when`(grpcTimeoutCertificate.highestQc).thenReturn(grpcHighestQc) // Return the mocked QuorumCertificate
        `when`(grpcTimeoutCertificate.signerIndices).thenReturn(ByteString.copyFrom(signerIndicesBytes))
        `when`(grpcTimeoutCertificate.sigData).thenReturn(ByteString.copyFrom(sigDataBytes))

        val flowTimeoutCertificate = FlowTimeoutCertificate.of(grpcTimeoutCertificate)

        assertEquals(view, flowTimeoutCertificate.view)
        assertEquals(highQcViews, flowTimeoutCertificate.highQcViews)
        assertEquals(highestQc, flowTimeoutCertificate.highestQc)
        assertArrayEquals(signerIndicesBytes, flowTimeoutCertificate.signerIndices)
        assertArrayEquals(sigDataBytes, flowTimeoutCertificate.sigData)
    }

    @Test
    fun `test builder() method`() {
        // mock FlowQuorumCertificate
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

        // mock FlowTimeoutCertificate
        val highQcViews = listOf(1L, 2L)
        val signerIndices = byteArrayOf(1, 2)
        val sigData = byteArrayOf(3, 4)

        val timeoutCertificate = FlowTimeoutCertificate(
            view = view,
            highQcViews = highQcViews,
            highestQc = flowQuorumCertificate,
            signerIndices = signerIndices,
            sigData = sigData
        )

        val result = timeoutCertificate.builder().build()

        assertEquals(view, result.view)
        assertEquals(highQcViews, result.highQcViewsList)
        assertEquals(ByteString.copyFrom(signerIndices), result.signerIndices)
        assertEquals(ByteString.copyFrom(sigData), result.sigData)
        assertEquals(flowQuorumCertificate.builder().build(), result.highestQc)
    }

    @Test
    fun `test equals() method`() {
        val view1 = 123L
        val highQcViews1 = listOf(456L, 789L)
        val signerIndices1 = ByteArray(32) { 0x01 }
        val sigData1 = ByteArray(32) { 0x02 }

        val view2 = 124L
        val highQcViews2 = listOf(111L, 222L)
        val signerIndices2 = ByteArray(32) { 0x03 }
        val sigData2 = ByteArray(32) { 0x04 }

        val highestQc1 = mock(FlowQuorumCertificate::class.java)
        val highestQc2 = mock(FlowQuorumCertificate::class.java)

        val flowTimeoutCertificate1 = FlowTimeoutCertificate(view1, highQcViews1, highestQc1, signerIndices1, sigData1)
        val flowTimeoutCertificate2 = FlowTimeoutCertificate(view1, highQcViews1, highestQc1, signerIndices1, sigData1)
        val flowTimeoutCertificate3 = FlowTimeoutCertificate(view2, highQcViews2, highestQc2, signerIndices2, sigData2)

        assertEquals(flowTimeoutCertificate1, flowTimeoutCertificate2)
        assertNotEquals(flowTimeoutCertificate1, flowTimeoutCertificate3)
    }

    @Test
    fun `test hashCode() method`() {
        val view1 = 123L
        val highQcViews1 = listOf(456L, 789L)
        val signerIndices1 = ByteArray(32) { 0x01 }
        val sigData1 = ByteArray(32) { 0x02 }

        val view2 = 124L
        val highQcViews2 = listOf(111L, 222L)
        val signerIndices2 = ByteArray(32) { 0x03 }
        val sigData2 = ByteArray(32) { 0x04 }

        val highestQc1 = mock(FlowQuorumCertificate::class.java)
        val highestQc2 = mock(FlowQuorumCertificate::class.java)

        val flowTimeoutCertificate1 = FlowTimeoutCertificate(view1, highQcViews1, highestQc1, signerIndices1, sigData1)
        val flowTimeoutCertificate2 = FlowTimeoutCertificate(view1, highQcViews1, highestQc1, signerIndices1, sigData1)
        val flowTimeoutCertificate3 = FlowTimeoutCertificate(view2, highQcViews2, highestQc2, signerIndices2, sigData2)

        assertEquals(flowTimeoutCertificate1.hashCode(), flowTimeoutCertificate2.hashCode())
        assertNotEquals(flowTimeoutCertificate1.hashCode(), flowTimeoutCertificate3.hashCode())
    }
}
