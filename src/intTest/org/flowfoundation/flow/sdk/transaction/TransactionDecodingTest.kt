package org.flowfoundation.flow.sdk.transaction

import org.flowfoundation.flow.sdk.*
import org.assertj.core.api.Assertions.assertThat
import org.flowfoundation.flow.sdk.test.FlowEmulatorTest
import org.junit.jupiter.api.Test

@FlowEmulatorTest
class TransactionDecodingTest {
    @Test
    fun `Can decode transaction envelope`() {
        // the value below was calculated using the flow-go-sdk from the tx defined here https://github.com/onflow/flow-go-sdk/blob/3ecd5d4920939922bb3b010b0d1b5567131b1341/transaction_test.go#L119-L129
        val canonicalTransactionHex = "f882f872b07472616e73616374696f6e207b2065786563757465207b206c6f67282248656c6c6f2c20576f726c64212229207d207dc0a001020000000000000000000000000000000000000000000000000000000000002a88f8d6e0586b0a20c7032a88ee82856bf20e2aa6c988f8d6e0586b0a20c7c8c3800202c3800301c4c3010703"
        val expectedTransactionId = "d1a2c58aebfce1050a32edf3568ec3b69cb8637ae090b5f7444ca6b2a8de8f8b"
        val proposerAddress = "f8d6e0586b0a20c7"
        val payerAddress = "ee82856bf20e2aa6"

        val decodedTx = FlowTransaction.of(canonicalTransactionHex.hexToBytes())

        assertThat(decodedTx.script).isEqualTo(FlowScript("transaction { execute { log(\"Hello, World!\") } }"))
        assertThat(decodedTx.arguments).isEqualTo(emptyList<FlowArgument>())
        assertThat(decodedTx.referenceBlockId).isEqualTo(FlowId.of(byteArrayOf(1, 2).copyOf(32)))
        assertThat(decodedTx.gasLimit).isEqualTo(42)
        assertThat(decodedTx.proposalKey.address.base16Value).isEqualTo(proposerAddress)
        assertThat(decodedTx.proposalKey.keyIndex).isEqualTo(3)
        assertThat(decodedTx.proposalKey.sequenceNumber).isEqualTo(42)
        assertThat(decodedTx.payerAddress.base16Value).isEqualTo(payerAddress)
        assertThat(decodedTx.authorizers).isEqualTo(listOf(FlowAddress(proposerAddress)))

        assertThat(decodedTx.payloadSignatures).isEqualTo(
            listOf(
                FlowTransactionSignature(FlowAddress("f8d6e0586b0a20c7"), 0, 2, FlowSignature(byteArrayOf(2))),
                FlowTransactionSignature(
                    FlowAddress("f8d6e0586b0a20c7"), 0, 3, FlowSignature(byteArrayOf(1))
                )
            )
        )
        assertThat(decodedTx.envelopeSignatures).isEqualTo(
            listOf(FlowTransactionSignature(FlowAddress(payerAddress), 1, 7, FlowSignature(byteArrayOf(3))))
        )

        assertThat(decodedTx.id.base16Value).isEqualTo(expectedTransactionId)
        assertThat(decodedTx.canonicalTransaction.bytesToHex()).isEqualTo(canonicalTransactionHex)
    }

    @Test
    fun `Can precompute the transaction id`() {
        // the example below was retrieved from https://github.com/onflow/flow-go-sdk/blob/3ecd5d4920939922bb3b010b0d1b5567131b1341/transaction_test.go#L119-L129
        val expectedTransactionIdBeforeSigning = "8c362dd8b7553d48284cecc94d2ab545d513b29f930555632390fff5ca9772ee"
        val expectedTransactionIdAfterSigning = "d1a2c58aebfce1050a32edf3568ec3b69cb8637ae090b5f7444ca6b2a8de8f8b"
        val expectedCanonicalTransactionHex = "f882f872b07472616e73616374696f6e207b2065786563757465207b206c6f67282248656c6c6f2c20576f726c64212229207d207dc0a001020000000000000000000000000000000000000000000000000000000000002a88f8d6e0586b0a20c7032a88ee82856bf20e2aa6c988f8d6e0586b0a20c7c8c3800202c3800301c4c3010703"
        val proposerAddress = "f8d6e0586b0a20c7"
        val payerAddress = "ee82856bf20e2aa6"

        var testTx = FlowTransaction(
            script = FlowScript("transaction { execute { log(\"Hello, World!\") } }"),
            arguments = emptyList(),
            referenceBlockId = FlowId.of(byteArrayOf(1, 2).copyOf(32)),
            gasLimit = 42,
            proposalKey = FlowTransactionProposalKey(
                address = FlowAddress(proposerAddress),
                keyIndex = 3,
                sequenceNumber = 42
            ),
            payerAddress = FlowAddress(payerAddress),
            authorizers = listOf(FlowAddress(proposerAddress))
        )

        assertThat(testTx.id.base16Value).isEqualTo(expectedTransactionIdBeforeSigning)

        testTx = testTx.addPayloadSignature(FlowAddress(proposerAddress), 3, FlowSignature(byteArrayOf(1)))
        testTx = testTx.addPayloadSignature(FlowAddress(proposerAddress), 2, FlowSignature(byteArrayOf(2)))
        testTx = testTx.addEnvelopeSignature(FlowAddress(payerAddress), 7, FlowSignature(byteArrayOf(3)))

        assertThat(testTx.id.base16Value).isEqualTo(expectedTransactionIdAfterSigning)
        assertThat(testTx.canonicalTransaction.bytesToHex()).isEqualTo(expectedCanonicalTransactionHex)
    }
}
