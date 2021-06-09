package org.onflow.sdk

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.onflow.sdk.crypto.Crypto
import org.onflow.sdk.test.FlowEmulatorTest

const val MAINNET_HOSTNAME = "access.mainnet.nodes.onflow.org"

@FlowEmulatorTest
class TransactionTest {

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
    fun `Can sign transactions`() {

        val pk1 = Crypto.getSigner(Crypto.generateKeyPair().private)
        val pk2 = Crypto.getSigner(Crypto.generateKeyPair().private)
        val pk3 = Crypto.getSigner(Crypto.generateKeyPair().private)

        val proposer = transaction.proposalKey.address
        val authorizer = FlowAddress("012345678012345678")
        val payer = FlowAddress("ababababababababab")

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

        val address = FlowAddress("f8d6e0586b0a20c7")

        val fooSignature = byteArrayOf(4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4)
        val barSignature = byteArrayOf(3, 3, 3)

        val signedTx = transaction.copy(
            payloadSignatures = listOf(
                FlowTransactionSignature(address, 0, 0, FlowSignature(fooSignature)),
                FlowTransactionSignature(address, 4, 5, FlowSignature(barSignature))
            )
        )

        val authorizationEnvelope = signedTx.canonicalAuthorizationEnvelope
        assertThat(authorizationEnvelope).isEqualTo(envelopeExpectedHex.hexToBytes())
    }

    @Test
    fun `Can connect to mainnet`() {

        val accessAPI = Flow.newAccessApi(MAINNET_HOSTNAME)
        accessAPI.ping()

        val address = FlowAddress("e467b9dd11fa00df")
        val account = accessAPI.getAccountAtLatestBlock(address)
        assertThat(account).isNotNull
        println(account!!)
        assertThat(account.keys).isNotEmpty
    }

    // ignored for now because for whatever reason it can't find this transaction
    @Test
    fun `Can parse events`() {
        val accessApi = Flow.newAccessApi(MAINNET_HOSTNAME)

        // https://flowscan.org/transaction/5e6ef76c524dd131bbab5f9965493b7830bb784561ca6391b320ec60fa5c395e
        val tx = accessApi.getTransactionById(FlowId("5e6ef76c524dd131bbab5f9965493b7830bb784561ca6391b320ec60fa5c395e"))
        assertThat(tx).isNotNull

        val results = accessApi.getTransactionResultById(FlowId("5e6ef76c524dd131bbab5f9965493b7830bb784561ca6391b320ec60fa5c395e"))!!
        assertThat(results.events).hasSize(4)
        assertThat(results.events[0].event.id).isEqualTo("A.0b2a3299cc857e29.TopShot.Withdraw")
        assertThat(results.events[1].event.id).isEqualTo("A.c1e4f4f4c4257510.Market.CutPercentageChanged")
        assertThat(results.events[2].event.id).isEqualTo("A.0b2a3299cc857e29.TopShot.Deposit")

        assertThat(results.events[3].event.id).isEqualTo("A.c1e4f4f4c4257510.Market.MomentListed")
        assertThat("id" in results.events[3].event).isTrue
        assertThat("price" in results.events[3].event).isTrue
        assertThat("seller" in results.events[3].event).isTrue
        assertThat("id" in results.events[3].event.value!!).isTrue
        assertThat("price" in results.events[3].event.value!!).isTrue
        assertThat("seller" in results.events[3].event.value!!).isTrue
    }

    @Test
    fun `Can create an account using the transaction DSL`() {
        val accessAPI = Flow.newAccessApi("localhost", 3570)

        val latestBlockId = accessAPI.getLatestBlockHeader().id

        val payerAccount = accessAPI.getAccountAtLatestBlock(FlowAddress("f8d6e0586b0a20c7"))!!

        val keyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        val payerSigner = Crypto.getSigner(keyPair.private, payerAccount.keys[0].hashAlgo)

        val newAccountPublicKey = FlowAccountKey(
            publicKey = FlowPublicKey(keyPair.public.hex),
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

            authorizers {
                address(payerAccount.address)
            }

            envelopeSignatures {
                signature {
                    address = payerAccount.address
                    keyIndex = 0
                    signer = payerSigner
                }
            }
        }

        val txID = accessAPI.sendTransaction(tx)
        val result = waitForSeal(accessAPI, txID)
        assertThat(result).isNotNull
        assertThat(result.status).isEqualTo(FlowTransactionStatus.SEALED)
    }

    @Test
    fun `Can create an account using the simpleTransaction DSL`() {
        val accessAPI = Flow.newAccessApi("localhost", 3570)
        val keyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        val payerSigner = Crypto.getSigner(keyPair.private, HashAlgorithm.SHA3_256)

        val result = accessAPI.simpleFlowTransaction(FlowAddress("f8d6e0586b0a20c7"), payerSigner) {
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
                arg { string(keyPair.public.hex) }
            }
        }.sendAndWaitForSeal()
        assertThat(result.status).isEqualTo(FlowTransactionStatus.SEALED)
    }

    @Test
    fun `Can get block header by id`() {
        val accessAPI = Flow.newAccessApi(MAINNET_HOSTNAME)

        val latestBlock = accessAPI.getLatestBlock(true)
        assertThat(latestBlock).isNotNull
        val blockHeaderById = accessAPI.getBlockHeaderById(latestBlock.id)
        assertThat(blockHeaderById).isNotNull
    }

    @Test
    fun `Can get block header by height`() {
        val accessAPI = Flow.newAccessApi(MAINNET_HOSTNAME)

        val latestBlock = accessAPI.getLatestBlock(true)
        assertThat(latestBlock).isNotNull
        val blockHeader = accessAPI.getBlockHeaderByHeight(latestBlock.height)
        assertThat(blockHeader).isNotNull
        assertThat(blockHeader?.height).isEqualTo(latestBlock.height)
    }

    @Test
    fun `Can get latest block`() {
        val accessAPI = Flow.newAccessApi(MAINNET_HOSTNAME)

        val latestBlock = accessAPI.getLatestBlock(true)
        assertThat(latestBlock).isNotNull
    }

    @Test
    fun `Can get block by id`() {
        val accessAPI = Flow.newAccessApi(MAINNET_HOSTNAME)

        val latestBlock = accessAPI.getLatestBlock(true)
        assertThat(latestBlock).isNotNull

        val blockById = accessAPI.getBlockById(latestBlock.id)
        assertThat(blockById).isNotNull
        assertThat(blockById?.id).isEqualTo(latestBlock.id)
    }

    @Test
    fun `Can get block by height`() {
        val accessAPI = Flow.newAccessApi(MAINNET_HOSTNAME)

        val latestBlock = accessAPI.getLatestBlock(true)
        assertThat(latestBlock).isNotNull

        val blockByHeight = accessAPI.getBlockByHeight(latestBlock.height)
        assertThat(blockByHeight).isNotNull
        assertThat(blockByHeight?.height).isEqualTo(latestBlock.height)
    }

    @Test
    fun `Can get account by address`() {
        val accessAPI = Flow.newAccessApi(MAINNET_HOSTNAME)

        val address = FlowAddress("18eb4ee6b3c026d2")
        val account = accessAPI.getAccountAtLatestBlock(address)!!
        assertThat(account).isNotNull
        assertThat(account.address).isEqualTo(address)
        assertThat(account).isEqualTo(account)
    }

}
