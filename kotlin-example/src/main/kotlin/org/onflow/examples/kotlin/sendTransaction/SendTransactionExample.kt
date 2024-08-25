package org.onflow.examples.kotlin.sendTransaction

import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.examples.kotlin.ExamplesUtils
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.StringField
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.crypto.PrivateKey

internal class SendTransactionExample(
    privateKey: PrivateKey,
    accessApiConnection: FlowAccessApi
) {
    private val privateKey = privateKey
    private val accessAPI = accessApiConnection

    private val connector = AccessAPIConnector(privateKey, accessAPI)

    fun sendSimpleTransaction(
        payerAddress: FlowAddress,
        scriptName: String = "cadence/simple_transaction.cdc",
        gasLimit: Long = 500
    ): FlowTransactionResult {
        val payerAccountKey = connector.getAccountKey(payerAddress, 0)

        var tx = FlowTransaction(
            script = FlowScript(ExamplesUtils.loadScript(scriptName)),
            arguments = listOf(),
            referenceBlockId = connector.latestBlockID,
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

        val txResult = connector.waitForSeal(txID)
        return txResult
    }

    fun sendComplexTransactionWithArguments(
        payerAddress: FlowAddress,
        scriptName: String = "cadence/greeting_script.cdc",
        gasLimit: Long = 500,
        greeting: String =  "Hello world!"
    ): FlowTransactionResult {

        val payerAccountKey = connector.getAccountKey(payerAddress, 0)

        var tx = FlowTransaction(
            script = FlowScript(ExamplesUtils.loadScript(scriptName)),
            arguments = listOf(
                FlowArgument(StringField(greeting)),
            ),
            referenceBlockId = connector.latestBlockID,
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

        val txResult = connector.waitForSeal(txID)
        return txResult
    }
}
