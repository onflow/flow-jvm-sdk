package org.onflow.flow.sdk.models

import com.google.protobuf.ByteString
import org.onflow.flow.sdk.FlowCollectionGuarantee
import org.onflow.flow.sdk.FlowId
import org.onflow.flow.sdk.FlowSignature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.onflow.protobuf.entities.CollectionOuterClass

class FlowCollectionGuaranteeTest {
    @Test
    fun `Test building FlowCollectionGuarantee from CollectionOuterClass`() {
        val collectionIdBytes = byteArrayOf(1, 2, 3)
        val signature1Bytes = byteArrayOf(4, 5, 6)
        val signature2Bytes = byteArrayOf(7, 8, 9)

        val collectionGuaranteeBuilder = CollectionOuterClass.CollectionGuarantee
            .newBuilder()
            .setCollectionId(ByteString.copyFrom(collectionIdBytes))
            .addSignatures(ByteString.copyFrom(signature1Bytes))
            .addSignatures(ByteString.copyFrom(signature2Bytes))

        val flowCollectionGuarantee = FlowCollectionGuarantee.of(collectionGuaranteeBuilder.build())

        assertEquals(FlowId.of(collectionIdBytes), flowCollectionGuarantee.id)
        assertEquals(2, flowCollectionGuarantee.signatures.size)
        assertEquals(FlowSignature(signature1Bytes), flowCollectionGuarantee.signatures[0])
        assertEquals(FlowSignature(signature2Bytes), flowCollectionGuarantee.signatures[1])
    }

    @Test
    fun `Test building CollectionOuterClass from FlowCollectionGuarantee`() {
        val collectionId = FlowId.of(byteArrayOf(1, 2, 3))
        val signatures = listOf(FlowSignature(byteArrayOf(4, 5, 6)), FlowSignature(byteArrayOf(7, 8, 9)))

        val flowCollectionGuarantee = FlowCollectionGuarantee(collectionId, signatures)

        val collectionGuaranteeBuilder = flowCollectionGuarantee.builder()
        val collectionGuarantee = collectionGuaranteeBuilder.build()

        assertEquals(ByteString.copyFrom(collectionId.bytes), collectionGuarantee.collectionId)
        assertEquals(2, collectionGuarantee.signaturesCount)
        assertEquals(ByteString.copyFrom(signatures[0].bytes), collectionGuarantee.getSignatures(0))
        assertEquals(ByteString.copyFrom(signatures[1].bytes), collectionGuarantee.getSignatures(1))
    }
}
