package com.nftco.flow.sdk

import com.google.protobuf.ByteString
import com.nftco.flow.sdk.FlowCollection
import com.nftco.flow.sdk.FlowId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.onflow.protobuf.entities.CollectionOuterClass

class FlowCollectionTest {
    @Test
    fun `Test building FlowCollection from CollectionOuterClass`() {
        val collectionIdBytes = byteArrayOf(1, 2, 3)
        val transaction1IdBytes = byteArrayOf(4, 5, 6)
        val transaction2IdBytes = byteArrayOf(7, 8, 9)

        val collectionBuilder = CollectionOuterClass.Collection.newBuilder()
            .setId(ByteString.copyFrom(collectionIdBytes))
            .addTransactionIds(ByteString.copyFrom(transaction1IdBytes))
            .addTransactionIds(ByteString.copyFrom(transaction2IdBytes))

        val flowCollection = FlowCollection.of(collectionBuilder.build())

        assertEquals(FlowId.of(collectionIdBytes), flowCollection.id)
        assertEquals(2, flowCollection.transactionIds.size)
        assertEquals(FlowId.of(transaction1IdBytes), flowCollection.transactionIds[0])
        assertEquals(FlowId.of(transaction2IdBytes), flowCollection.transactionIds[1])
    }

    @Test
    fun `Test building CollectionOuterClass from FlowCollection`() {
        val collectionId = FlowId.of(byteArrayOf(1, 2, 3))
        val transactionIds = listOf(FlowId.of(byteArrayOf(4, 5, 6)), FlowId.of(byteArrayOf(7, 8, 9)))

        val flowCollection = FlowCollection(collectionId, transactionIds)

        val collectionBuilder = flowCollection.builder()
        val collection = collectionBuilder.build()

        assertEquals(ByteString.copyFrom(collectionId.bytes), collection.id)
        assertEquals(2, collection.transactionIdsCount)
        assertEquals(ByteString.copyFrom(transactionIds[0].bytes), collection.getTransactionIds(0))
        assertEquals(ByteString.copyFrom(transactionIds[1].bytes), collection.getTransactionIds(1))
    }
}
