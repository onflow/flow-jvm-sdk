package org.onflow.examples.kotlin

import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.AddressField
import org.onflow.flow.sdk.cadence.StringField
import org.onflow.flow.sdk.cadence.UFix64NumberField
import org.onflow.flow.sdk.crypto.Crypto
import java.math.BigDecimal

internal class AccessAPIConnector(privateKeyHex: String, accessApiConnection: FlowAccessApi) {
    private val accessAPI = accessApiConnection
    private val privateKey = Crypto.decodePrivateKey(privateKeyHex)

    private val latestBlockID: FlowId
        get() = when (val response = accessAPI.getLatestBlockHeader()) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data.id
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

    private fun getAccount(address: FlowAddress): FlowAccount {
        return when (val response = accessAPI.getAccountAtLatestBlock(address)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
    }

    fun getAccountBalance(address: FlowAddress): BigDecimal {
        val account = getAccount(address)
        return account.balance
    }

    private fun getAccountKey(address: FlowAddress, keyIndex: Int): FlowAccountKey {
        val account = getAccount(address)
        return account.keys[keyIndex]
    }

    private fun getTransactionResult(txID: FlowId): FlowTransactionResult {
        return when (val response = accessAPI.getTransactionResultById(txID)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> {
                if (response.data.errorMessage.isNotEmpty()) {
                    throw Exception(response.data.errorMessage)
                }
                response.data
            }
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
    }

    private fun waitForSeal(txID: FlowId): FlowTransactionResult {
        while (true) {
            val txResult = getTransactionResult(txID)
            if (txResult.status == FlowTransactionStatus.SEALED) {
                return txResult
            }
            Thread.sleep(1000)
        }
    }

    private fun getAccountCreatedAddress(txResult: FlowTransactionResult): FlowAddress {
        val addressHex = txResult
            .events[0]
            .event
            .value!!
            .fields[0]
            .value
            .value as String
        return FlowAddress(addressHex.substring(2).split(".")[0])
    }

    private fun loadScript(name: String): ByteArray = javaClass.classLoader.getResourceAsStream(name)!!.use { it.readAllBytes() }

    fun createAccount(payerAddress: FlowAddress, publicKeyHex: String): FlowAddress {
        val payerAccountKey = getAccountKey(payerAddress, 0)

        var tx = FlowTransaction(
            script = FlowScript(loadScript("cadence/create_account.cdc")),
            arguments = listOf(
                FlowArgument(StringField(publicKeyHex))
            ),
            referenceBlockId = latestBlockID,
            gasLimit = 100,
            proposalKey = FlowTransactionProposalKey(
                address = payerAddress,
                keyIndex = payerAccountKey.id,
                sequenceNumber = payerAccountKey.sequenceNumber.toLong()
            ),
            payerAddress = payerAddress,
            authorizers = listOf(payerAddress)
        )

        val signer = Crypto.getSigner(privateKey, payerAccountKey.hashAlgo)
        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey.id, signer)

        val txID = when (val response = accessAPI.sendTransaction(tx)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

        val txResult = waitForSeal(txID)
        return getAccountCreatedAddress(txResult)
    }

    fun transferTokens(senderAddress: FlowAddress, recipientAddress: FlowAddress, amount: BigDecimal) {
        if (amount.scale() != 8) {
            throw Exception("FLOW amount must have exactly 8 decimal places of precision (e.g. 10.00000000)")
        }
        val senderAccountKey = getAccountKey(senderAddress, 0)

        var tx = FlowTransaction(
            script = FlowScript(loadScript("cadence/transfer_flow.cdc")),
            arguments = listOf(
                FlowArgument(UFix64NumberField(amount.toPlainString())),
                FlowArgument(AddressField(recipientAddress.base16Value))
            ),
            referenceBlockId = latestBlockID,
            gasLimit = 100,
            proposalKey = FlowTransactionProposalKey(
                address = senderAddress,
                keyIndex = senderAccountKey.id,
                sequenceNumber = senderAccountKey.sequenceNumber.toLong()
            ),
            payerAddress = senderAddress,
            authorizers = listOf(senderAddress)
        )

        val signer = Crypto.getSigner(privateKey, senderAccountKey.hashAlgo)
        tx = tx.addEnvelopeSignature(senderAddress, senderAccountKey.id, signer)

        val txID = when (val response = accessAPI.sendTransaction(tx)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

        waitForSeal(txID)
    }
}
