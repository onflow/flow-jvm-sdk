package org.onflow.flow.sdk.models

import com.google.protobuf.ByteString
import org.onflow.flow.sdk.FlowCollectionGuarantee
import org.onflow.flow.sdk.FlowId
import org.onflow.flow.sdk.FlowSignature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.onflow.protobuf.entities.CollectionOuterClass
import com.google.protobuf.UnsafeByteOperations

class FlowCollectionGuaranteeTest {
    @Test
    fun `Test building FlowCollectionGuarantee from CollectionOuterClass`() {
        val collectionIdBytes = byteArrayOf(1, 2, 3)
        val referenceBlockIdBytes = byteArrayOf(10, 11, 12)
        val signatureBytes = byteArrayOf(13, 14, 15)
        val signature1Bytes = byteArrayOf(4, 5, 6)
        val signature2Bytes = byteArrayOf(7, 8, 9)
        val signerIndicesBytes = byteArrayOf(16, 17)

        val collectionGuaranteeBuilder = CollectionOuterClass.CollectionGuarantee.newBuilder()
            .setCollectionId(ByteString.copyFrom(collectionIdBytes))
            .addSignatures(ByteString.copyFrom(signature1Bytes))
            .addSignatures(ByteString.copyFrom(signature2Bytes))
            .setReferenceBlockId(ByteString.copyFrom(referenceBlockIdBytes))
            .setSignature(ByteString.copyFrom(signatureBytes))
            .setSignerIndices(UnsafeByteOperations.unsafeWrap(signerIndicesBytes))

        val flowCollectionGuarantee = FlowCollectionGuarantee.of(collectionGuaranteeBuilder.build())

        assertEquals(FlowId.of(collectionIdBytes), flowCollectionGuarantee.id)
        assertEquals(2, flowCollectionGuarantee.signatures.size)
        assertEquals(FlowSignature(signature1Bytes), flowCollectionGuarantee.signatures[0])
        assertEquals(FlowSignature(signature2Bytes), flowCollectionGuarantee.signatures[1])
        assertEquals(FlowId.of(referenceBlockIdBytes), flowCollectionGuarantee.referenceBlockId)
        assertEquals(FlowSignature(signatureBytes), flowCollectionGuarantee.signature)
        assertEquals(signerIndicesBytes.toList(), flowCollectionGuarantee.signerIndices.toList())
    }

    @Test
    fun `Test building CollectionOuterClass from FlowCollectionGuarantee`() {
        val collectionId = FlowId.of(byteArrayOf(1, 2, 3))
        val referenceBlockId = FlowId.of(byteArrayOf(10, 11, 12))
        val signature = FlowSignature(byteArrayOf(13, 14, 15))
        val signatures = listOf(FlowSignature(byteArrayOf(4, 5, 6)), FlowSignature(byteArrayOf(7, 8, 9)))
        val signerIndices = byteArrayOf(16, 17)

        val flowCollectionGuarantee = FlowCollectionGuarantee(collectionId, signatures, referenceBlockId, signature, signerIndices)

        val collectionGuaranteeBuilder = flowCollectionGuarantee.builder()
        val collectionGuarantee = collectionGuaranteeBuilder.build()

        assertEquals(ByteString.copyFrom(collectionId.bytes), collectionGuarantee.collectionId)
        assertEquals(2, collectionGuarantee.signaturesCount)
        assertEquals(ByteString.copyFrom(signatures[0].bytes), collectionGuarantee.getSignatures(0))
        assertEquals(ByteString.copyFrom(signatures[1].bytes), collectionGuarantee.getSignatures(1))
        assertEquals(ByteString.copyFrom(referenceBlockId.bytes), collectionGuarantee.referenceBlockId)
        assertEquals(ByteString.copyFrom(signature.bytes), collectionGuarantee.signature)
        assertEquals(ByteString.copyFrom(signerIndices), collectionGuarantee.signerIndices)
    }
}
