package org.onflow.flow.sdk.transaction

import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.IntegrationTestUtils.transaction
import org.assertj.core.api.Assertions.assertThat
import org.onflow.flow.sdk.test.FlowEmulatorTest
import org.onflow.flow.sdk.test.FlowServiceAccountCredentials
import org.onflow.flow.sdk.test.FlowTestClient
import org.onflow.flow.sdk.test.TestAccount
import org.junit.jupiter.api.Test

@FlowEmulatorTest
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
        val latestBlockId = when (val latestBlockHeaderResult = accessAPI.getLatestBlockHeader()) {
            is FlowAccessApi.FlowResult.Success -> latestBlockHeaderResult.data.id
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get latest block header: ${latestBlockHeaderResult.message}", latestBlockHeaderResult.throwable)
        }

        val payerAccount = when (val payerAccountResult = accessAPI.getAccountAtLatestBlock(serviceAccount.flowAddress)) {
            is FlowAccessApi.FlowResult.Success -> payerAccountResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to get account at latest block: ${payerAccountResult.message}", payerAccountResult.throwable)
        }

        val newAccountKeyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        val newAccountPublicKey = FlowAccountKey(
            publicKey = FlowPublicKey(newAccountKeyPair.public.hex),
            signAlgo = SignatureAlgorithm.ECDSA_P256,
            hashAlgo = HashAlgorithm.SHA3_256,
            weight = 1000
        )

        val tx = flowTransaction {
            script {
                """
                transaction(publicKey: String) {
                    prepare(signer: AuthAccount) {
                        let account = AuthAccount(payer: signer)
                        account.addPublicKey(publicKey.decodeHex())
                    }
                }
            """
            }

            arguments {
                arg { string(newAccountPublicKey.encoded.bytesToHex()) }
            }

            referenceBlockId = latestBlockId
            gasLimit = 100

            proposalKey {
                address = payerAccount.address
                keyIndex = payerAccount.keys[0].id
                sequenceNumber = payerAccount.keys[0].sequenceNumber.toLong()
            }

            payerAddress = payerAccount.address

            signatures {
                signature {
                    address = payerAccount.address
                    keyIndex = 0
                    signer = serviceAccount.signer
                }
            }
        }

        val txID = when (val sendTransactionResult = accessAPI.sendTransaction(tx)) {
            is FlowAccessApi.FlowResult.Success -> sendTransactionResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to send transaction: ${sendTransactionResult.message}", sendTransactionResult.throwable)
        }

        val result = when (val waitForSealResult = waitForSeal(accessAPI, txID)) {
            is FlowAccessApi.FlowResult.Success -> waitForSealResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to wait for seal: ${waitForSealResult.message}", waitForSealResult.throwable)
        }

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

        val transactionResult = accessAPI.simpleFlowTransaction(serviceAccount.flowAddress, serviceAccount.signer) {
            script {
                """
                transaction(publicKey: String) {
                    prepare(signer: AuthAccount) {
                        let account = AuthAccount(payer: signer)
                        account.addPublicKey(publicKey.decodeHex())
                    }
                }
            """
            }

            arguments {
                arg { string(newAccountPublicKey.encoded.bytesToHex()) }
            }
        }.sendAndWaitForSeal()

        val result = when (transactionResult) {
            is FlowAccessApi.FlowResult.Success -> transactionResult.data
            is FlowAccessApi.FlowResult.Error -> throw IllegalStateException("Failed to create account: ${transactionResult.message}", transactionResult.throwable)
        }

        assertThat(result.status).isEqualTo(FlowTransactionStatus.SEALED)
    }
}
