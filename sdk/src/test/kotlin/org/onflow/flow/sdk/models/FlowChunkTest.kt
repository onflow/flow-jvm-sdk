package org.onflow.flow.sdk.models

import com.google.protobuf.ByteString
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.onflow.flow.sdk.FlowChunk
import org.onflow.flow.sdk.FlowId
import org.onflow.protobuf.entities.ExecutionResultOuterClass

class FlowChunkTest {
    @Test
    fun `test of() method`() {
        val collectionIndex = 1
        val startStateBytes = ByteArray(32) { 0x01 }
        val eventCollectionBytes = ByteArray(32) { 0x02 }
        val blockIdBytes = ByteArray(32) { 0x03 }
        val totalComputationUsed = 1000L
        val numberOfTransactions = 10
        val index = 5L
        val endStateBytes = ByteArray(32) { 0x04 }
        val executionDataIdBytes = ByteArray(32) { 0x05 }
        val stateDeltaCommitmentBytes = ByteArray(32) { 0x06 }

        val grpcChunk = mock(ExecutionResultOuterClass.Chunk::class.java)
        `when`(grpcChunk.collectionIndex).thenReturn(collectionIndex)
        `when`(grpcChunk.startState).thenReturn(ByteString.copyFrom(startStateBytes))
        `when`(grpcChunk.eventCollection).thenReturn(ByteString.copyFrom(eventCollectionBytes))
        `when`(grpcChunk.blockId).thenReturn(ByteString.copyFrom(blockIdBytes))
        `when`(grpcChunk.totalComputationUsed).thenReturn(totalComputationUsed)
        `when`(grpcChunk.numberOfTransactions).thenReturn(numberOfTransactions)
        `when`(grpcChunk.index).thenReturn(index)
        `when`(grpcChunk.endState).thenReturn(ByteString.copyFrom(endStateBytes))
        `when`(grpcChunk.executionDataId).thenReturn(ByteString.copyFrom(executionDataIdBytes))
        `when`(grpcChunk.stateDeltaCommitment).thenReturn(ByteString.copyFrom(stateDeltaCommitmentBytes))

        val flowChunk = FlowChunk.of(grpcChunk)

        assertEquals(collectionIndex, flowChunk.collectionIndex)
        assertArrayEquals(startStateBytes, flowChunk.startState)
        assertArrayEquals(eventCollectionBytes, flowChunk.eventCollection)
        assertEquals(FlowId.of(blockIdBytes), flowChunk.blockId)
        assertEquals(totalComputationUsed, flowChunk.totalComputationUsed)
        assertEquals(numberOfTransactions, flowChunk.numberOfTransactions)
        assertEquals(index, flowChunk.index)
        assertArrayEquals(endStateBytes, flowChunk.endState)
        assertEquals(FlowId.of(executionDataIdBytes), flowChunk.executionDataId)
        assertArrayEquals(stateDeltaCommitmentBytes, flowChunk.stateDeltaCommitment)
    }

    @Test
    fun `test builder() method`() {
        val collectionIndex = 1
        val startStateBytes = ByteArray(32) { 0x01 }
        val eventCollectionBytes = ByteArray(32) { 0x02 }
        val blockId = FlowId.of(ByteArray(32) { 0x03 })
        val totalComputationUsed = 1000L
        val numberOfTransactions = 10
        val index = 5L
        val endStateBytes = ByteArray(32) { 0x04 }
        val executionDataId = FlowId.of(ByteArray(32) { 0x05 })
        val stateDeltaCommitmentBytes = ByteArray(32) { 0x06 }

        val flowChunk = FlowChunk(
            collectionIndex = collectionIndex,
            startState = startStateBytes,
            eventCollection = eventCollectionBytes,
            blockId = blockId,
            totalComputationUsed = totalComputationUsed,
            numberOfTransactions = numberOfTransactions,
            index = index,
            endState = endStateBytes,
            executionDataId = executionDataId,
            stateDeltaCommitment = stateDeltaCommitmentBytes
        )

        val result = flowChunk.builder().build()

        assertEquals(collectionIndex, result.collectionIndex)
        assertEquals(ByteString.copyFrom(startStateBytes), result.startState)
        assertEquals(ByteString.copyFrom(eventCollectionBytes), result.eventCollection)
        assertEquals(blockId.byteStringValue, result.blockId)
        assertEquals(totalComputationUsed, result.totalComputationUsed)
        assertEquals(numberOfTransactions, result.numberOfTransactions)
        assertEquals(index, result.index)
        assertEquals(ByteString.copyFrom(endStateBytes), result.endState)
        assertEquals(executionDataId.byteStringValue, result.executionDataId)
        assertEquals(ByteString.copyFrom(stateDeltaCommitmentBytes), result.stateDeltaCommitment)
    }

    @Test
    fun `test equals() method`() {
        val collectionIndex1 = 1
        val collectionIndex2 = 2
        val startState1 = ByteArray(32) { 0x01 }
        val startState2 = ByteArray(32) { 0x02 }
        val blockId1 = FlowId.of(ByteArray(32) { 0x03 })
        val blockId2 = FlowId.of(ByteArray(32) { 0x04 })
        val totalComputationUsed1 = 1000L
        val totalComputationUsed2 = 2000L
        val endState1 = ByteArray(32) { 0x04 }
        val endState2 = ByteArray(32) { 0x05 }

        val flowChunk1 = FlowChunk(
            collectionIndex = collectionIndex1,
            startState = startState1,
            eventCollection = startState1,
            blockId = blockId1,
            totalComputationUsed = totalComputationUsed1,
            numberOfTransactions = 10,
            index = 5L,
            endState = endState1,
            executionDataId = blockId1,
            stateDeltaCommitment = startState1
        )

        val flowChunk2 = FlowChunk(
            collectionIndex = collectionIndex1,
            startState = startState1,
            eventCollection = startState1,
            blockId = blockId1,
            totalComputationUsed = totalComputationUsed1,
            numberOfTransactions = 10,
            index = 5L,
            endState = endState1,
            executionDataId = blockId1,
            stateDeltaCommitment = startState1
        )

        val flowChunk3 = FlowChunk(
            collectionIndex = collectionIndex2,
            startState = startState2,
            eventCollection = startState2,
            blockId = blockId2,
            totalComputationUsed = totalComputationUsed2,
            numberOfTransactions = 20,
            index = 10L,
            endState = endState2,
            executionDataId = blockId2,
            stateDeltaCommitment = startState2
        )

        assertEquals(flowChunk1, flowChunk2)
        assertNotEquals(flowChunk1, flowChunk3)
    }

    @Test
    fun `test hashCode() method`() {
        val collectionIndex1 = 1
        val collectionIndex2 = 2
        val startState1 = ByteArray(32) { 0x01 }
        val startState2 = ByteArray(32) { 0x02 }
        val blockId1 = FlowId.of(ByteArray(32) { 0x03 })
        val blockId2 = FlowId.of(ByteArray(32) { 0x04 })
        val totalComputationUsed1 = 1000L
        val totalComputationUsed2 = 2000L
        val endState1 = ByteArray(32) { 0x04 }
        val endState2 = ByteArray(32) { 0x05 }

        val flowChunk1 = FlowChunk(
            collectionIndex = collectionIndex1,
            startState = startState1,
            eventCollection = startState1,
            blockId = blockId1,
            totalComputationUsed = totalComputationUsed1,
            numberOfTransactions = 10,
            index = 5L,
            endState = endState1,
            executionDataId = blockId1,
            stateDeltaCommitment = startState1
        )

        val flowChunk2 = FlowChunk(
            collectionIndex = collectionIndex1,
            startState = startState1,
            eventCollection = startState1,
            blockId = blockId1,
            totalComputationUsed = totalComputationUsed1,
            numberOfTransactions = 10,
            index = 5L,
            endState = endState1,
            executionDataId = blockId1,
            stateDeltaCommitment = startState1
        )

        val flowChunk3 = FlowChunk(
            collectionIndex = collectionIndex2,
            startState = startState2,
            eventCollection = startState2,
            blockId = blockId2,
            totalComputationUsed = totalComputationUsed2,
            numberOfTransactions = 20,
            index = 10L,
            endState = endState2,
            executionDataId = blockId2,
            stateDeltaCommitment = startState2
        )

        assertEquals(flowChunk1.hashCode(), flowChunk2.hashCode())
        assertNotEquals(flowChunk1.hashCode(), flowChunk3.hashCode())
    }
}

