package org.onflow.flow.sdk.models

import com.google.protobuf.ByteString
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.onflow.flow.sdk.*
import org.onflow.protobuf.access.Access
import org.onflow.protobuf.entities.ExecutionResultOuterClass

class FlowExecutionResultTest {
    companion object {
        val collectionIndex = 1
        val startStateBytes = ByteArray(32) { 0x01 }
        val eventCollectionBytes = ByteArray(32) { 0x02 }
        val totalComputationUsed = 1000L
        val numberOfTransactions = 10
        val index = 5L
        val endStateBytes = ByteArray(32) { 0x04 }
        val executionDataIdBytes = ByteArray(32) { 0x05 }
        val stateDeltaCommitmentBytes = ByteArray(32) { 0x06 }
        val payloadBytes = ByteArray(32) { 0x07 }

        val blockIdBytes = ByteArray(32) { 0x01 }
        val previousResultIdBytes = ByteArray(32) { 0x02 }
    }

    @Test
    fun `test of() method with ExecutionResultByIDResponse`() {
        // Mock inner ExecutionResult
        val grpcExecutionResultInner = mock(ExecutionResultOuterClass.ExecutionResult::class.java)
        `when`(grpcExecutionResultInner.blockId).thenReturn(ByteString.copyFrom(blockIdBytes))
        `when`(grpcExecutionResultInner.previousResultId).thenReturn(ByteString.copyFrom(previousResultIdBytes))

        // Mock flow chunk
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

        `when`(grpcExecutionResultInner.chunksList).thenReturn(listOf(grpcChunk))

        // Mock service event
        val serviceEvent = mock(ExecutionResultOuterClass.ServiceEvent::class.java)
        `when`(serviceEvent.type).thenReturn("EventType")
        `when`(serviceEvent.payload).thenReturn(ByteString.copyFrom(payloadBytes))
        `when`(grpcExecutionResultInner.serviceEventsList).thenReturn(listOf(serviceEvent))

        // Mock outer Access.ExecutionResultByIDResponse
        val grpcExecutionResult = mock(Access.ExecutionResultByIDResponse::class.java)
        `when`(grpcExecutionResult.executionResult).thenReturn(grpcExecutionResultInner)

        val flowExecutionResult = FlowExecutionResult.of(grpcExecutionResult)

        assertEquals(FlowId.of(blockIdBytes), flowExecutionResult.blockId)
        assertEquals(FlowId.of(previousResultIdBytes), flowExecutionResult.previousResultId)
        assertEquals(1, flowExecutionResult.chunks.size)
        assertEquals(1, flowExecutionResult.serviceEvents.size)
        assertEquals("EventType", flowExecutionResult.serviceEvents[0].type)
        assertArrayEquals(payloadBytes, flowExecutionResult.serviceEvents[0].payload)
    }

    @Test
    fun `test of() method with ExecutionResult`() {
        // Mock flow chunk
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

        // Mock service event
        val serviceEvent = mock(ExecutionResultOuterClass.ServiceEvent::class.java)
        `when`(serviceEvent.type).thenReturn("EventType")
        `when`(serviceEvent.payload).thenReturn(ByteString.copyFrom(payloadBytes))

        // Mock ExecutionResult
        val grpcExecutionResult = mock(ExecutionResultOuterClass.ExecutionResult::class.java)
        `when`(grpcExecutionResult.blockId).thenReturn(ByteString.copyFrom(blockIdBytes))
        `when`(grpcExecutionResult.previousResultId).thenReturn(ByteString.copyFrom(previousResultIdBytes))
        `when`(grpcExecutionResult.chunksList).thenReturn(listOf(grpcChunk))
        `when`(grpcExecutionResult.serviceEventsList).thenReturn(listOf(serviceEvent))

        val flowExecutionResult = FlowExecutionResult.of(grpcExecutionResult)

        assertEquals(FlowId.of(blockIdBytes), flowExecutionResult.blockId)
        assertEquals(FlowId.of(previousResultIdBytes), flowExecutionResult.previousResultId)
        assertEquals(1, flowExecutionResult.chunks.size)
        assertEquals(1, flowExecutionResult.serviceEvents.size)
        assertEquals("EventType", flowExecutionResult.serviceEvents[0].type)
        assertArrayEquals(payloadBytes, flowExecutionResult.serviceEvents[0].payload)
    }

    @Test
    fun `test builder() method`() {
        // Setup mock FlowServiceEvent
        val serviceEvent = FlowServiceEvent("EventType", blockIdBytes)

        // Setup mock FlowChunk
        val flowChunk = FlowChunk(
            collectionIndex = collectionIndex,
            startState = startStateBytes,
            eventCollection = eventCollectionBytes,
            blockId = FlowId.of(blockIdBytes),
            totalComputationUsed = totalComputationUsed,
            numberOfTransactions = numberOfTransactions,
            index = index,
            endState = endStateBytes,
            executionDataId = FlowId.of(blockIdBytes),
            stateDeltaCommitment = stateDeltaCommitmentBytes
        )

        val flowExecutionResult = FlowExecutionResult(
            blockId = FlowId.of(blockIdBytes),
            previousResultId = FlowId.of(previousResultIdBytes),
            chunks = listOf(flowChunk),
            serviceEvents = listOf(serviceEvent)
        )

        val result = flowExecutionResult.builder().build()

        assertEquals(FlowId.of(blockIdBytes).byteStringValue, result.blockId)
        assertEquals(FlowId.of(previousResultIdBytes).byteStringValue, result.previousResultId)
        assertEquals(1, result.chunksCount)
        assertEquals(1, result.serviceEventsCount)
    }

    @Test
    fun `test equals() method`() {
        val blockId1 = FlowId.of(ByteArray(32) { 0x01 })
        val previousResultId1 = FlowId.of(ByteArray(32) { 0x02 })
        val chunk1 = mock(FlowChunk::class.java)
        val serviceEvent1 = mock(FlowServiceEvent::class.java)

        val blockId2 = FlowId.of(ByteArray(32) { 0x03 })
        val previousResultId2 = FlowId.of(ByteArray(32) { 0x04 })
        val chunk2 = mock(FlowChunk::class.java)
        val serviceEvent2 = mock(FlowServiceEvent::class.java)

        val flowExecutionResult1 = FlowExecutionResult(
            blockId = blockId1,
            previousResultId = previousResultId1,
            chunks = listOf(chunk1),
            serviceEvents = listOf(serviceEvent1)
        )

        val flowExecutionResult2 = FlowExecutionResult(
            blockId = blockId1,
            previousResultId = previousResultId1,
            chunks = listOf(chunk1),
            serviceEvents = listOf(serviceEvent1)
        )

        val flowExecutionResult3 = FlowExecutionResult(
            blockId = blockId2,
            previousResultId = previousResultId2,
            chunks = listOf(chunk2),
            serviceEvents = listOf(serviceEvent2)
        )

        assertEquals(flowExecutionResult1, flowExecutionResult2)
        assertNotEquals(flowExecutionResult1, flowExecutionResult3)
    }

    @Test
    fun `test hashCode() method`() {
        val blockId1 = FlowId.of(ByteArray(32) { 0x01 })
        val previousResultId1 = FlowId.of(ByteArray(32) { 0x02 })
        val chunk1 = mock(FlowChunk::class.java)
        val serviceEvent1 = mock(FlowServiceEvent::class.java)

        val blockId2 = FlowId.of(ByteArray(32) { 0x03 })
        val previousResultId2 = FlowId.of(ByteArray(32) { 0x04 })
        val chunk2 = mock(FlowChunk::class.java)
        val serviceEvent2 = mock(FlowServiceEvent::class.java)

        val flowExecutionResult1 = FlowExecutionResult(
            blockId = blockId1,
            previousResultId = previousResultId1,
            chunks = listOf(chunk1),
            serviceEvents = listOf(serviceEvent1)
        )

        val flowExecutionResult2 = FlowExecutionResult(
            blockId = blockId1,
            previousResultId = previousResultId1,
            chunks = listOf(chunk1),
            serviceEvents = listOf(serviceEvent1)
        )

        val flowExecutionResult3 = FlowExecutionResult(
            blockId = blockId2,
            previousResultId = previousResultId2,
            chunks = listOf(chunk2),
            serviceEvents = listOf(serviceEvent2)
        )

        assertEquals(flowExecutionResult1.hashCode(), flowExecutionResult2.hashCode())
        assertNotEquals(flowExecutionResult1.hashCode(), flowExecutionResult3.hashCode())
    }
}
