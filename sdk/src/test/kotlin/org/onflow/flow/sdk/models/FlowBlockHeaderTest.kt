package org.onflow.flow.sdk.models

import com.google.protobuf.ByteString
import com.google.protobuf.UnsafeByteOperations
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.onflow.flow.sdk.*
import org.onflow.protobuf.entities.BlockHeaderOuterClass
import java.time.LocalDateTime

class FlowBlockHeaderTest {
    companion object {
        val idBytes = ByteArray(32) { 0x01 }
        val parentIdBytes = ByteArray(32) { 0x02 }
        val proposerIdBytes = ByteArray(32) { 0x03 }
        val payloadHashBytes = ByteArray(32) { 0x04 }
        val voterSigDataBytes = ByteArray(32) { 0x05 }
        val proposerSigDataBytes = ByteArray(32) { 0x06 }
        val voterIndicesBytes = ByteArray(32) { 0x07 }

        val id = FlowId.of(idBytes)
        val parentId = FlowId.of(parentIdBytes)
        val proposerId = FlowId.of(proposerIdBytes)
        val chainId = FlowChainId.of("mainnet")
        val timestamp: LocalDateTime = LocalDateTime.now()
        val timeoutCertificate: FlowTimeoutCertificate = mock(FlowTimeoutCertificate::class.java)
    }

    @Test
    fun `test of() method`() {
        val chainId = "mainnet"

        // Mock block header
        val grpcBlockHeader = mock(BlockHeaderOuterClass.BlockHeader::class.java)
        `when`(grpcBlockHeader.id).thenReturn(ByteString.copyFrom(idBytes))
        `when`(grpcBlockHeader.parentId).thenReturn(ByteString.copyFrom(parentIdBytes))
        `when`(grpcBlockHeader.height).thenReturn(123L)
        `when`(grpcBlockHeader.timestamp).thenReturn(timestamp.asTimestamp())
        `when`(grpcBlockHeader.payloadHash).thenReturn(ByteString.copyFrom(payloadHashBytes))
        `when`(grpcBlockHeader.view).thenReturn(456L)
        `when`(grpcBlockHeader.parentVoterSigData).thenReturn(ByteString.copyFrom(voterSigDataBytes))
        `when`(grpcBlockHeader.proposerId).thenReturn(ByteString.copyFrom(proposerIdBytes))
        `when`(grpcBlockHeader.proposerSigData).thenReturn(ByteString.copyFrom(proposerSigDataBytes))
        `when`(grpcBlockHeader.chainId).thenReturn(chainId)
        `when`(grpcBlockHeader.parentVoterIndices).thenReturn(ByteString.copyFrom(voterIndicesBytes))

        // Mock timeout certificate and quorum certificate
        val timeoutCertificateMock = mock(BlockHeaderOuterClass.TimeoutCertificate::class.java)
        `when`(timeoutCertificateMock.signerIndices).thenReturn(ByteString.copyFrom(idBytes))
        `when`(timeoutCertificateMock.sigData).thenReturn(ByteString.copyFrom(idBytes))

        val highestQcMock = mock(BlockHeaderOuterClass.QuorumCertificate::class.java)
        `when`(highestQcMock.blockId).thenReturn(ByteString.copyFrom(idBytes))
        `when`(highestQcMock.signerIndices).thenReturn(ByteString.copyFrom(idBytes))
        `when`(highestQcMock.sigData).thenReturn(ByteString.copyFrom(idBytes))

        `when`(grpcBlockHeader.lastViewTc).thenReturn(timeoutCertificateMock)
        `when`(timeoutCertificateMock.highestQc).thenReturn(highestQcMock)
        `when`(grpcBlockHeader.parentView).thenReturn(789L)

        val flowBlockHeader = FlowBlockHeader.of(grpcBlockHeader)

        assertEquals(FlowId.of(idBytes), flowBlockHeader.id)
        assertEquals(FlowId.of(parentIdBytes), flowBlockHeader.parentId)
        assertEquals(123L, flowBlockHeader.height)
        assertEquals(timestamp, flowBlockHeader.timestamp)
        assertArrayEquals(payloadHashBytes, flowBlockHeader.payloadHash)
        assertEquals(456L, flowBlockHeader.view)
        assertArrayEquals(voterSigDataBytes, flowBlockHeader.parentVoterSigData)
        assertEquals(FlowId.of(proposerIdBytes), flowBlockHeader.proposerId)
        assertArrayEquals(proposerSigDataBytes, flowBlockHeader.proposerSigData)
        assertEquals(FlowChainId.of(chainId), flowBlockHeader.chainId)
        assertArrayEquals(voterIndicesBytes, flowBlockHeader.parentVoterIndices)
        assertEquals(789L, flowBlockHeader.parentView)
    }

    @Test
    fun `test builder() method`() {
        // Mock FlowQuorumCertificate
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

        // Mock FlowTimeoutCertificate
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

        val flowBlockHeader = FlowBlockHeader(
            id = id,
            parentId = parentId,
            height = 123L,
            timestamp = timestamp,
            payloadHash = payloadHashBytes,
            view = 456L,
            parentVoterSigData = voterSigDataBytes,
            proposerId = proposerId,
            proposerSigData = proposerSigDataBytes,
            chainId = chainId,
            parentVoterIndices = voterIndicesBytes,
            lastViewTc = timeoutCertificate,
            parentView = 789L
        )

        val result = flowBlockHeader.builder().build()

        assertEquals(id.byteStringValue, result.id)
        assertEquals(parentId.byteStringValue, result.parentId)
        assertEquals(123L, result.height)
        assertEquals(timestamp.asTimestamp(), result.timestamp)
        assertEquals(UnsafeByteOperations.unsafeWrap(payloadHashBytes), result.payloadHash)
        assertEquals(456L, result.view)
        assertEquals(UnsafeByteOperations.unsafeWrap(voterSigDataBytes), result.parentVoterSigData)
        assertEquals(proposerId.byteStringValue, result.proposerId)
        assertEquals(UnsafeByteOperations.unsafeWrap(proposerSigDataBytes), result.proposerSigData)
        assertEquals(chainId.id, result.chainId)
        assertEquals(UnsafeByteOperations.unsafeWrap(voterIndicesBytes), result.parentVoterIndices)
        assertEquals(789L, result.parentView)
    }

    @Test
    fun `test equals() method`() {
        val flowBlockHeader1 = FlowBlockHeader(
            id = id,
            parentId = parentId,
            height = 123L,
            timestamp = timestamp,
            payloadHash = payloadHashBytes,
            view = 456L,
            parentVoterSigData = voterSigDataBytes,
            proposerId = proposerId,
            proposerSigData = proposerSigDataBytes,
            chainId = chainId,
            parentVoterIndices = voterIndicesBytes,
            lastViewTc = timeoutCertificate,
            parentView = 789L
        )

        val flowBlockHeader2 = FlowBlockHeader(
            id = id,
            parentId = parentId,
            height = 123L,
            timestamp = timestamp,
            payloadHash = payloadHashBytes,
            view = 456L,
            parentVoterSigData = voterSigDataBytes,
            proposerId = proposerId,
            proposerSigData = proposerSigDataBytes,
            chainId = chainId,
            parentVoterIndices = voterIndicesBytes,
            lastViewTc = timeoutCertificate,
            parentView = 789L
        )

        assertEquals(flowBlockHeader1, flowBlockHeader2)

        val flowBlockHeader3 = FlowBlockHeader(
            id = FlowId.of(ByteArray(32) { 0x08 }),
            parentId = parentId,
            height = 124L,
            timestamp = timestamp.plusDays(1),
            payloadHash = ByteArray(32) { 0x09 },
            view = 789L,
            parentVoterSigData = voterSigDataBytes,
            proposerId = proposerId,
            proposerSigData = proposerSigDataBytes,
            chainId = chainId,
            parentVoterIndices = voterIndicesBytes,
            lastViewTc = timeoutCertificate,
            parentView = 100L
        )

        assertNotEquals(flowBlockHeader1, flowBlockHeader3)
    }

    @Test
    fun `test hashCode() method`() {
        val flowBlockHeader1 = FlowBlockHeader(
            id = id,
            parentId = parentId,
            height = 123L,
            timestamp = timestamp,
            payloadHash = payloadHashBytes,
            view = 456L,
            parentVoterSigData = voterSigDataBytes,
            proposerId = proposerId,
            proposerSigData = proposerSigDataBytes,
            chainId = chainId,
            parentVoterIndices = voterIndicesBytes,
            lastViewTc = timeoutCertificate,
            parentView = 789L
        )

        val flowBlockHeader2 = FlowBlockHeader(
            id = id,
            parentId = parentId,
            height = 123L,
            timestamp = timestamp,
            payloadHash = payloadHashBytes,
            view = 456L,
            parentVoterSigData = voterSigDataBytes,
            proposerId = proposerId,
            proposerSigData = proposerSigDataBytes,
            chainId = chainId,
            parentVoterIndices = voterIndicesBytes,
            lastViewTc = timeoutCertificate,
            parentView = 789L
        )

        assertEquals(flowBlockHeader1.hashCode(), flowBlockHeader2.hashCode())

        val flowBlockHeader3 = FlowBlockHeader(
            id = FlowId.of(ByteArray(32) { 0x08 }),
            parentId = parentId,
            height = 124L,
            timestamp = timestamp.plusDays(1),
            payloadHash = ByteArray(32) { 0x09 },
            view = 789L,
            parentVoterSigData = voterSigDataBytes,
            proposerId = proposerId,
            proposerSigData = proposerSigDataBytes,
            chainId = chainId,
            parentVoterIndices = voterIndicesBytes,
            lastViewTc = timeoutCertificate,
            parentView = 100L
        )

        assertNotEquals(flowBlockHeader1.hashCode(), flowBlockHeader3.hashCode())
    }
}
