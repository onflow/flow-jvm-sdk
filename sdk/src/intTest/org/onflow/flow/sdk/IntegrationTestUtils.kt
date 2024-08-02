package org.onflow.flow.sdk

import org.onflow.flow.common.test.FlowTestUtil
import org.onflow.flow.common.test.TestAccount
import org.onflow.flow.sdk.cadence.AddressField
import org.onflow.flow.sdk.crypto.Crypto
import java.nio.charset.StandardCharsets

object IntegrationTestUtils {
    var transaction = FlowTransaction(
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

    fun <T> handleResult(result: FlowAccessApi.AccessApiCallResponse<T>, errorMessage: String): T {
        return when (result) {
            is FlowAccessApi.AccessApiCallResponse.Success -> result.data ?: throw IllegalStateException("$errorMessage: result data is null")
            is FlowAccessApi.AccessApiCallResponse.Error -> throw IllegalStateException("$errorMessage: ${result.message}", result.throwable)
        }
    }

    fun getAccountAddressFromResult(result: Any): FlowAddress {
        val addressField = (result as List<*>).find { it is AddressField } as? AddressField
        return addressField?.value?.let { FlowAddress(it) }
            ?: throw IllegalStateException("AccountCreated event not found")
    }

    fun getAccount(api: FlowAccessApi, address: FlowAddress): FlowAccount {
        val result = api.getAccountAtLatestBlock(address)
        return handleResult(result, "Failed to get account at latest block")
    }

    fun createAndSubmitAccountCreationTransaction(
        accessAPI: FlowAccessApi,
        serviceAccount: TestAccount,
        scriptPath: String
    ): FlowTransactionResult {
        val latestBlockId = getLatestBlockId(accessAPI)
        val payerAccount = getAccount(accessAPI, serviceAccount.flowAddress)

        val newAccountKeyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)

//        val newAccountPublicKey = FlowAccountKey(
//            publicKey = FlowPublicKey(newAccountKeyPair.public.hex),
//            signAlgo = SignatureAlgorithm.ECDSA_P256,
//            hashAlgo = HashAlgorithm.SHA3_256,
//            weight = 1000
//        )

        val loadedScript = String(FlowTestUtil.loadScript(scriptPath), StandardCharsets.UTF_8)

        val tx = flowTransaction {
            script {
                loadedScript
            }

            arguments {
                arg { string(newAccountKeyPair.public.hex) }
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

        val txID = handleResult(accessAPI.sendTransaction(tx), "Failed to send transaction")

        return handleResult(waitForSeal(accessAPI, txID), "Failed to wait for seal")
    }

    private fun getLatestBlockId(api: FlowAccessApi): FlowId {
        val result = api.getLatestBlockHeader()
        return handleResult(result, "Failed to get latest block header").id
    }
}
