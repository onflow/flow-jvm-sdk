package com.nftco.flow.sdk.transaction

import com.nftco.flow.sdk.*
import com.nftco.flow.sdk.crypto.Crypto
import com.nftco.flow.sdk.IntegrationTestUtils.transaction
import org.assertj.core.api.Assertions.assertThat
import com.nftco.flow.sdk.test.FlowEmulatorTest
import com.nftco.flow.sdk.test.FlowServiceAccountCredentials
import com.nftco.flow.sdk.test.FlowTestClient
import com.nftco.flow.sdk.test.TestAccount
import org.junit.jupiter.api.Assertions.*
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
        val latestBlockId = accessAPI.getLatestBlockHeader().id

        val payerAccount = accessAPI.getAccountAtLatestBlock(serviceAccount.flowAddress)!!

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

        val txID = accessAPI.sendTransaction(tx)
        val result = waitForSeal(accessAPI, txID).throwOnError()
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

        val result = accessAPI.simpleFlowTransaction(serviceAccount.flowAddress, serviceAccount.signer) {
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
            .throwOnError()
        assertThat(result.status).isEqualTo(FlowTransactionStatus.SEALED)
    }

    @Test
    fun `Payer address authorization is correct`() {
        val tx = transaction

        assertEquals(serviceAccount.flowAddress, tx.payerAddress)
    }

    @Test
    fun `Transaction script content is accurate`() {
        val tx = transaction

        assertEquals(
            "import 0xsomething \n {}",
            tx.script.stringValue
        )
    }

    @Test
    fun `Gas limit is properly handled`() {
        val gasLimits = listOf(100L, 200L, 300L)

        val transactions = gasLimits.map { limit ->
            transaction.copy(gasLimit = limit)
        }

        transactions.forEachIndexed { index, tx ->
            assertEquals(gasLimits[index], tx.gasLimit)
        }
    }
}
