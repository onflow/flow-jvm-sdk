package com.nftco.flow.sdk

import com.nftco.flow.sdk.crypto.Crypto
import com.nftco.flow.sdk.test.FlowEmulatorTest
import com.nftco.flow.sdk.test.FlowServiceAccountCredentials
import com.nftco.flow.sdk.test.FlowTestClient
import com.nftco.flow.sdk.test.TestAccount
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.random.Random

@FlowEmulatorTest
class TransactionTest {
    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    private var transaction = FlowTransaction(
        script = FlowScript("import 0xsomething \n {}"),
        arguments = listOf(FlowArgument(byteArrayOf(2, 2, 3)), FlowArgument(byteArrayOf(3, 3, 3))),
        referenceBlockId = FlowId.of(byteArrayOf(3, 3, 3, 6, 6, 6)),
        gasLimit = 44,
        proposalKey = FlowTransactionProposalKey(
            address = FlowAddress.of(byteArrayOf(4, 5, 4, 5, 4, 5)),
            keyIndex = 11,
            sequenceNumber = 7
        ),
        payerAddress = FlowAddress.of(byteArrayOf(6, 5, 4, 3, 2)),
        authorizers = listOf(FlowAddress.of(byteArrayOf(9, 9, 9, 9, 9)), FlowAddress.of(byteArrayOf(8, 9, 9, 9, 9)))
    )

    @Test
    fun `wut`() {
        val account = TestUtils.newTestnetAccessApi().getAccountAtLatestBlock(FlowAddress("0x6bd3869f2631beb3"))
        val x = account?.keys?.isEmpty()
    }

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
    fun `Can connect to mainnet`() {
        val accessAPI = TestUtils.newMainnetAccessApi()
        accessAPI.ping()

        val address = FlowAddress("e467b9dd11fa00df")
        val account = accessAPI.getAccountAtLatestBlock(address)
        assertThat(account).isNotNull
        println(account!!)
        assertThat(account.keys).isNotEmpty
    }

    @Test
    fun `Can parse events`() {
        val accessApi = TestUtils.newMainnetAccessApi()

        // https://flowscan.org/transaction/8c2e9d37a063240f236aa181e1454eb62991b42302534d4d6dd3839c2df0ef14
        val tx = accessApi.getTransactionById(FlowId("8c2e9d37a063240f236aa181e1454eb62991b42302534d4d6dd3839c2df0ef14"))
        assertThat(tx).isNotNull

        val results = accessApi.getTransactionResultById(FlowId("8c2e9d37a063240f236aa181e1454eb62991b42302534d4d6dd3839c2df0ef14"))!!
        assertThat(results.events).hasSize(12)
        assertThat(results.events[0].event.id).isEqualTo("A.0b2a3299cc857e29.TopShot.Withdraw")
        assertThat(results.events[1].event.id).isEqualTo("A.0b2a3299cc857e29.TopShot.Deposit")
        assertThat(results.events[2].event.id).isEqualTo("A.ead892083b3e2c6c.DapperUtilityCoin.TokensWithdrawn")

        assertThat("from" in results.events[2].event).isTrue
        assertThat("amount" in results.events[2].event).isTrue

        assertThat(results.events[8].event.id).isEqualTo("A.b8ea91944fd51c43.OffersV2.OfferCompleted")
        assertThat("nftId" in results.events[8].event).isTrue
        assertThat("nftType" in results.events[8].event).isTrue
        assertThat("offerId" in results.events[8].event).isTrue
        assertThat("offerType" in results.events[8].event.value!!).isTrue
        assertThat("royalties" in results.events[8].event.value!!).isTrue
        assertThat("offerAddress" in results.events[8].event.value!!).isTrue
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
    fun `Can get block header by id`() {
        val accessAPI = TestUtils.newMainnetAccessApi()

        val latestBlock = accessAPI.getLatestBlock(true)
        assertThat(latestBlock).isNotNull
        val blockHeaderById = accessAPI.getBlockHeaderById(latestBlock.id)
        assertThat(blockHeaderById).isNotNull
    }

    @Test
    fun `Can get block header by height`() {
        val accessAPI = TestUtils.newMainnetAccessApi()

        val latestBlock = accessAPI.getLatestBlock(true)
        assertThat(latestBlock).isNotNull
        val blockHeader = accessAPI.getBlockHeaderByHeight(latestBlock.height)
        assertThat(blockHeader).isNotNull
        assertThat(blockHeader?.height).isEqualTo(latestBlock.height)
    }

    @Test
    fun `Can get latest block`() {
        val accessAPI = TestUtils.newMainnetAccessApi()

        val latestBlock = accessAPI.getLatestBlock(true)
        assertThat(latestBlock).isNotNull
    }

    @Test
    fun `Can get block by id`() {
        val accessAPI = TestUtils.newMainnetAccessApi()

        val latestBlock = accessAPI.getLatestBlock(true)
        assertThat(latestBlock).isNotNull

        val blockById = accessAPI.getBlockById(latestBlock.id)
        assertThat(blockById).isNotNull
        assertThat(blockById?.id).isEqualTo(latestBlock.id)
    }

    @Test
    fun `Can get block by height`() {
        val accessAPI = TestUtils.newMainnetAccessApi()

        val latestBlock = accessAPI.getLatestBlock(true)
        assertThat(latestBlock).isNotNull

        val blockByHeight = accessAPI.getBlockByHeight(latestBlock.height)
        assertThat(blockByHeight).isNotNull
        assertThat(blockByHeight?.height).isEqualTo(latestBlock.height)
    }

    @Test
    fun `Can get account by address`() {
        val accessAPI = TestUtils.newMainnetAccessApi()

        val address = FlowAddress("18eb4ee6b3c026d2")
        val account = accessAPI.getAccountAtLatestBlock(address)!!
        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
        assertThat(account).isEqualTo(account)
    }

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

    @Test
    fun `bytes arrays are properly handled`() {
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
