package com.nftco.flow.sdk.models

import com.google.protobuf.ByteString
import com.google.protobuf.Timestamp
import com.nftco.flow.sdk.FlowBlock
import com.nftco.flow.sdk.FlowId
import com.nftco.flow.sdk.asTimestamp
import com.nftco.flow.sdk.fixedSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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

        val flowBlock = FlowBlock.of(blockBuilder.build())

        val expectedUtcDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(timestamp.seconds),
            ZoneOffset.UTC
        )

        val actualUtcDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(123456789L),
            ZoneOffset.UTC
        )

        assert(flowBlock.id.bytes.contentEquals(fixedSize("id".toByteArray(),32)))
        assert(flowBlock.parentId.bytes.contentEquals(fixedSize("parent_id".toByteArray(),32)))
        assertEquals(flowBlock.height, 123L)
        assertEquals(
            expectedUtcDateTime,
            actualUtcDateTime,
        )
        assert(flowBlock.collectionGuarantees.isEmpty())
        assert(flowBlock.blockSeals.isEmpty())
        assert(flowBlock.signatures.isEmpty())
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
            signatures = emptyList()
        )

        val blockBuilder = flowBlock.builder()

        assert(blockBuilder.id.toByteArray().contentEquals(fixedSize("id".toByteArray(),32)))
        assert(blockBuilder.parentId.toByteArray().contentEquals(fixedSize("parent_id".toByteArray(),32)))
        assert(blockBuilder.height == 123L)
        assert(blockBuilder.timestamp == flowBlock.timestamp.asTimestamp())
        assert(blockBuilder.collectionGuaranteesList.isEmpty())
        assert(blockBuilder.blockSealsList.isEmpty())
        assert(blockBuilder.signaturesList.isEmpty())
    }
}
