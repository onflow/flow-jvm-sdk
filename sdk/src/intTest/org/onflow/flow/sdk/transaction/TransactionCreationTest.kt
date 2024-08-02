package org.onflow.flow.sdk.transaction

import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.crypto.Crypto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.onflow.flow.common.test.*
import org.onflow.flow.sdk.IntegrationTestUtils.createAndSubmitAccountCreationTransaction
import org.onflow.flow.sdk.IntegrationTestUtils.handleResult
import org.onflow.flow.sdk.IntegrationTestUtils.transaction
import java.nio.charset.StandardCharsets

@FlowEmulatorProjectTest(flowJsonLocation = "../flow/flow.json")
class TransactionCreationTest {
    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @Test
    fun `Canonical transaction form is accurate`() {
        val payloadEnvelope = transaction.canonicalPayload

        // those values were generated from Go implementation for the same transaction input data
        val payloadExpectedHex =
            "f86a97696d706f7274203078736f6d657468696e67200a207b7dc88302020383030303a000000000000000000000000000000000000000000000000000000303030606062c8800000405040504050b07880000000605040302d2880000000909090909880000000809090909"
        val envelopeExpectedHex =
            "f883f86a97696d706f7274203078736f6d657468696e67200a207b7dc88302020383030303a000000000000000000000000000000000000000000000000000000303030606062c8800000405040504050b07880000000605040302d2880000000909090909880000000809090909d6ce80808b0404040404040404040404c6040583030303"

        assertThat(payloadEnvelope).isEqualTo(payloadExpectedHex.hexToBytes())

        val fooSignature = byteArrayOf(4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4)
        val barSignature = byteArrayOf(3, 3, 3)

        val signedTx = transaction.copy(
            payloadSignatures = listOf(
                FlowTransactionSignature(serviceAccount.flowAddress, 0, 0, FlowSignature(fooSignature)),
                FlowTransactionSignature(serviceAccount.flowAddress, 4, 5, FlowSignature(barSignature))
            )
        )

        val authorizationEnvelope = signedTx.canonicalAuthorizationEnvelope
        assertThat(authorizationEnvelope).isEqualTo(envelopeExpectedHex.hexToBytes())
    }

    @Test
    fun `Can create an account using the transaction DSL`() {
        val result = createAndSubmitAccountCreationTransaction(
            accessAPI,
            serviceAccount,
            "cadence/transaction_creation/transaction_creation.cdc"
        )

        assertThat(result).isNotNull
        assertThat(result.status).isEqualTo(FlowTransactionStatus.SEALED)
    }

    @Test
    fun `Can create an account using the simpleTransaction DSL`() {
        val newAccountKeyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        val newAccountPublicKey = FlowAccountKey(
            publicKey = FlowPublicKey(newAccountKeyPair.public.hex),
            signAlgo = SignatureAlgorithm.ECDSA_P256,
            hashAlgo = HashAlgorithm.SHA3_256,
            weight = 1000
        )

        val loadedScript = String(FlowTestUtil.loadScript("cadence/transaction_creation/transaction_creation_simple_transaction.cdc"), StandardCharsets.UTF_8)

        val transactionResult = accessAPI.simpleFlowTransaction(serviceAccount.flowAddress, serviceAccount.signer) {
            script {
                loadedScript
            }

            arguments {
                arg { string(newAccountPublicKey.encoded.bytesToHex()) }
            }
        }.sendAndWaitForSeal()

        val result = handleResult(transactionResult, "Failed to create account")

        assertThat(result.status).isEqualTo(FlowTransactionStatus.SEALED)
    }
}
