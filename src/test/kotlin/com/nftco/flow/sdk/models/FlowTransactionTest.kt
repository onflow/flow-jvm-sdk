package com.nftco.flow.sdk.models

import com.google.protobuf.ByteString
import com.nftco.flow.sdk.*
import com.nftco.flow.sdk.cadence.StringField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.onflow.protobuf.entities.TransactionOuterClass

class FlowTransactionTest {

    @Test
    fun `Test construction from TransactionOuterClass`() {
        val transactionOuterClass = TransactionOuterClass.Transaction.newBuilder()
            .setScript(ByteString.copyFromUtf8("sample script"))
            .addAllArguments(listOf(ByteString.copyFromUtf8("argument1"), ByteString.copyFromUtf8("argument2")))
            .setReferenceBlockId(ByteString.copyFromUtf8("referenceBlockId"))
            .setGasLimit(1000)
            .setProposalKey(
                TransactionOuterClass.Transaction.ProposalKey.newBuilder()
                    .setAddress(ByteString.copyFromUtf8("proposalKeyAddress"))
                    .setKeyId(123)
                    .setSequenceNumber(456)
                    .build()
            )
            .setPayer(ByteString.copyFromUtf8("payerAddress"))
            .addAllAuthorizers(listOf(ByteString.copyFromUtf8("authorizer1"), ByteString.copyFromUtf8("authorizer2")))
            .addAllPayloadSignatures(
                listOf(
//                    TransactionOuterClass.Transaction.Signature.newBuilder()
//                        .setKeyId(1)
//                       // .setSignerIndex(0)
//                       // .setKeyIndex(1)
//                        .setSignature(ByteString.copyFromUtf8("signature1"))
//                        .build(),
//                    TransactionOuterClass.Transaction.Signature.newBuilder()
//                        .setKeyId(2)
//                        //.setSignerIndex(1)
//                        //.setKeyIndex(2)
//                        .setSignature(ByteString.copyFromUtf8("signature2"))
//                        .build()
                )
            )
            .addAllEnvelopeSignatures(
                listOf(
//                    TransactionOuterClass.Transaction.Signature.newBuilder()
//                        .setKeyId(1)
//                        //.setSignerIndex(0)
//                        //.setKeyIndex(1)
//                        .setSignature(ByteString.copyFromUtf8("signature1"))
//                        .build(),
//                    TransactionOuterClass.Transaction.Signature.newBuilder()
//                        .setKeyId(2)
//                        //.setSignerIndex(1)
//                        //.setKeyIndex(2)
//                        .setSignature(ByteString.copyFromUtf8("signature2"))
//                        .build()
                )
            )
            .build()

        val flowTransaction = FlowTransaction.of(transactionOuterClass.toByteArray())

        assertEquals("sample script", flowTransaction.script.bytes.toString(Charsets.UTF_8))
    }

    @Test
    fun `Test builder`() {
        val flowTransaction = FlowTransaction(
            FlowScript("sample script"),
            listOf(FlowArgument(StringField("argument"))),
            FlowId("0x1234"),
            1000L,
            FlowTransactionProposalKey(FlowAddress.of("0x01".hexToBytes()), 0, 12345L),
            FlowAddress.of("0x02".hexToBytes()),
            listOf(FlowAddress.of("0x03".hexToBytes()))
        )

        val builder = flowTransaction.builder()
        val builtTransaction = builder.build()

        assertEquals("sample script", builtTransaction.script.toString(Charsets.UTF_8))
        assertEquals(FlowArgument(StringField("argument")).byteStringValue, builtTransaction.argumentsList[0])
        assertEquals(FlowId("0x1234").byteStringValue, builtTransaction.referenceBlockId)
        assertEquals(1000L, builtTransaction.gasLimit)
        assertEquals(true, FlowAddress.of("0x01".hexToBytes()).bytes.contentEquals(builtTransaction.proposalKey.address.toByteArray()))
        assertEquals(0, builtTransaction.proposalKey.keyId)
        assertEquals(12345L, builtTransaction.proposalKey.sequenceNumber)
        assertEquals(true, FlowAddress.of("0x02".hexToBytes()).bytes.contentEquals(builtTransaction.payer.toByteArray()))
        assertEquals(true, FlowAddress.of("0x03".hexToBytes()).bytes.contentEquals(builtTransaction.authorizersList[0].toByteArray()))
    }

}
