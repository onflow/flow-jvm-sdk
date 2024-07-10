package org.onflow.flow.sdk.transaction

import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.FlowAddress
import org.onflow.flow.sdk.IntegrationTestUtils.transaction
import org.onflow.flow.sdk.bytesToHex
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.simpleFlowTransaction
import org.onflow.flow.sdk.test.FlowEmulatorTest
import org.onflow.flow.sdk.test.FlowServiceAccountCredentials
import org.onflow.flow.sdk.test.FlowTestClient
import org.onflow.flow.sdk.test.TestAccount
import org.junit.jupiter.api.Test
import kotlin.random.Random

@FlowEmulatorTest
class TransactionSigningTest {
    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @Test
    fun `Can sign transactions`() {
        val pk1 = Crypto.getSigner(Crypto.generateKeyPair().private)
        val pk2 = Crypto.getSigner(Crypto.generateKeyPair().private)
        val pk3 = Crypto.getSigner(Crypto.generateKeyPair().private)

        val proposer = transaction.proposalKey.address
        val authorizer = FlowAddress("0x18eb4ee6b3c026d3")
        val payer = FlowAddress("0xd550da24ebb66d75")

        transaction = transaction.addPayloadSignature(proposer, 2, pk1)
        println("Authorization signature (proposer) ${transaction.payloadSignatures[0].signature.base16Value}")
        println("Authorization envelope (proposer) ${transaction.canonicalAuthorizationEnvelope.bytesToHex()}")

        transaction = transaction.addPayloadSignature(authorizer, 3, pk2)
        println("Authorization signature (authorizer) ${transaction.payloadSignatures[0].signature.base16Value}")
        println("Authorization envelope (authorizer) ${transaction.canonicalAuthorizationEnvelope.bytesToHex()}")

        transaction = transaction.addEnvelopeSignature(payer, 5, pk3)
        println("Payment signature (payer) ${transaction.envelopeSignatures[0].signature.base16Value}")
        println("Payment envelope (payer) ${transaction.canonicalPaymentEnvelope.bytesToHex()}")
    }

    @Test
    fun `Byte arrays are properly handled`() {
        accessAPI.simpleFlowTransaction(serviceAccount.flowAddress, serviceAccount.signer) {
            script {
                """
                    transaction(bytes: [UInt8]) {
                        prepare(signer: AuthAccount) {
                            log(bytes)
                        }
                    }
                """.trimIndent()
            }
            arguments {
                arg { byteArray(Random.nextBytes(2048)) }
            }
        }.send()
            .waitForSeal()
            .throwOnError()
    }
}
