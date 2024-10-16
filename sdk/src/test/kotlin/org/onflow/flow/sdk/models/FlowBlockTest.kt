package org.onflow.flow.sdk.models

import com.google.protobuf.ByteString
import com.google.protobuf.Timestamp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.onflow.flow.sdk.*
import org.onflow.protobuf.entities.BlockHeaderOuterClass
import org.onflow.protobuf.entities.BlockOuterClass
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class FlowBlockTest {
    @Test
    fun `Test initialization from BlockOuterClass Block`() {
        val unixTimestamp = 123456789L
        val timestamp = Timestamp.newBuilder().setSeconds(unixTimestamp).build()

        val blockBuilder = BlockOuterClass.Block.newBuilder()
            .setId(ByteString.copyFromUtf8("id"))
            .setParentId(ByteString.copyFromUtf8("parent_id"))
            .setHeight(123)
            .setTimestamp(timestamp)
            .setBlockHeader(
                BlockHeaderOuterClass.BlockHeader.newBuilder()
                    .setId(ByteString.copyFromUtf8("header_id"))
                    .setParentId(ByteString.copyFromUtf8("header_parent_id"))
                    .setHeight(124)
                    .setTimestamp(timestamp)
                    .build()
            )
            .setProtocolStateId(ByteString.copyFromUtf8("protocol_state_id"))

        val flowBlock = FlowBlock.of(blockBuilder.build())

        val expectedUtcDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(timestamp.seconds),
            ZoneOffset.UTC
        )

        val actualUtcDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(123456789L),
            ZoneOffset.UTC
        )

        assert(flowBlock.id.bytes.contentEquals(fixedSize("id".toByteArray(), 32)))
        assert(flowBlock.parentId.bytes.contentEquals(fixedSize("parent_id".toByteArray(), 32)))
        assertEquals(flowBlock.height, 123L)
        assertEquals(expectedUtcDateTime, actualUtcDateTime)
        assert(flowBlock.collectionGuarantees.isEmpty())
        assert(flowBlock.blockSeals.isEmpty())
        assert(flowBlock.signatures.isEmpty())
        assert(flowBlock.executionReceiptMetaList.isEmpty())
        assert(flowBlock.executionResultList.isEmpty())
        assert(flowBlock.blockHeader.id.bytes.contentEquals(fixedSize("header_id".toByteArray(), 32)))
        assert(flowBlock.protocolStateId.bytes.contentEquals(fixedSize("protocol_state_id".toByteArray(), 32)))
    }

    @Test
    fun `Test builder`() {
        val flowBlock = FlowBlock(
            id = FlowId.of("id".toByteArray()),
            parentId = FlowId.of("parent_id".toByteArray()),
            height = 123,
            timestamp = LocalDateTime.now(),
            collectionGuarantees = emptyList(),
            blockSeals = emptyList(),
            signatures = emptyList(),
            executionReceiptMetaList = emptyList(),
            executionResultList = emptyList(),
            blockHeader = FlowBlockHeader(
                id = FlowId.of("header_id".toByteArray()),
                parentId = FlowId.of("header_parent_id".toByteArray()),
                height = 124,
                timestamp = LocalDateTime.now(),
                payloadHash = ByteArray(32),
                view = 1,
                parentVoterSigData = ByteArray(32),
                proposerId = FlowId.of("proposer_id".toByteArray()),
                proposerSigData = ByteArray(32),
                chainId = FlowChainId.MAINNET,
                parentVoterIndices = ByteArray(32),
                lastViewTc = FlowTimeoutCertificate(1L, emptyList(), FlowQuorumCertificate(1L, FlowId.of("block_id".toByteArray()), ByteArray(32), ByteArray(32)), ByteArray(32), ByteArray(32)),
                parentView = 1
            ),
            protocolStateId = FlowId.of("protocol_state_id".toByteArray())
        )

        val blockBuilder = flowBlock.builder()

        assert(blockBuilder.id.toByteArray().contentEquals(fixedSize("id".toByteArray(), 32)))
        assert(blockBuilder.parentId.toByteArray().contentEquals(fixedSize("parent_id".toByteArray(), 32)))
        assert(blockBuilder.height == 123L)
        assert(blockBuilder.timestamp.equals(flowBlock.timestamp.asTimestamp()))
        assert(blockBuilder.collectionGuaranteesList.isEmpty())
        assert(blockBuilder.blockSealsList.isEmpty())
        assert(blockBuilder.signaturesList.isEmpty())
        assert(blockBuilder.blockHeader.id.toByteArray().contentEquals(fixedSize("header_id".toByteArray(), 32)))
        assert(blockBuilder.protocolStateId.toByteArray().contentEquals(fixedSize("protocol_state_id".toByteArray(), 32)))
    }
}
