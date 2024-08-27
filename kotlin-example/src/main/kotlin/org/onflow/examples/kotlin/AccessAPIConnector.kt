package org.onflow.examples.kotlin

import org.onflow.examples.kotlin.ExamplesUtils.loadScript
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.*
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.crypto.PrivateKey
import org.onflow.flow.sdk.crypto.PublicKey
import java.math.BigDecimal

internal class AccessAPIConnector(
    privateKey: PrivateKey,
    accessApiConnection: FlowAccessApi
) {
    private val privateKey = privateKey
    private val accessAPI = accessApiConnection

    val latestBlockID: FlowId
        get() = when (val response = accessAPI.getLatestBlockHeader()) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data.id
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

    fun getAccount(address: FlowAddress): FlowAccount = when (val response = accessAPI.getAccountAtLatestBlock(address)) {
        is FlowAccessApi.AccessApiCallResponse.Success -> response.data
        is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
    }

    fun getAccountBalance(address: FlowAddress): BigDecimal {
        val account = getAccount(address)
        return account.balance
    }

    fun getAccountKey(address: FlowAddress, keyIndex: Int): FlowAccountKey {
        val account = getAccount(address)
        return account.keys[keyIndex]
    }

    private fun getTransactionResult(txID: FlowId): FlowTransactionResult = when (val response = accessAPI.getTransactionResultById(txID)) {
        is FlowAccessApi.AccessApiCallResponse.Success -> {
            if (response.data.errorMessage.isNotEmpty()) {
                throw Exception(response.data.errorMessage)
            }
            response.data
        }
        is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
    }

    fun waitForSeal(txID: FlowId): FlowTransactionResult {
        while (true) {
            val txResult = getTransactionResult(txID)
            if (txResult.status == FlowTransactionStatus.SEALED) {
                return txResult
            }
            Thread.sleep(1000)
        }
    }

    fun sendSampleTransaction(
        payerAddress: FlowAddress,
        publicKey: PublicKey,
        scriptName: String = "cadence/create_account.cdc",
        gasLimit: Long = 500
    ): FlowId {
        val payerAccountKey = getAccountKey(payerAddress, 0)

        var tx = FlowTransaction(
            script = FlowScript(loadScript(scriptName)),
            arguments = listOf(
                FlowArgument(StringField(publicKey.hex)),
                FlowArgument(UInt8NumberField(publicKey.algo.index.toString()))
            ),
            referenceBlockId = latestBlockID,
            gasLimit = gasLimit,
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

        return txID
    }

    fun transferTokens(senderAddress: FlowAddress, recipientAddress: FlowAddress, amount: BigDecimal) {
        if (amount.scale() != 8) {
            throw Exception("FLOW amount must have exactly 8 decimal places of precision (e.g. 10.00000000)")
        }
        val senderAccountKey = getAccountKey(senderAddress, 0)
        senderAccountKey.publicKey.bytes.bytesToHex()

        var tx = FlowTransaction(
            script = FlowScript(loadScript("cadence/transfer_flow.cdc")),
            arguments = listOf(
                FlowArgument(UFix64NumberField(amount.toPlainString())),
                FlowArgument(AddressField(recipientAddress.base16Value))
            ),
            referenceBlockId = latestBlockID,
            gasLimit = 500,
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
