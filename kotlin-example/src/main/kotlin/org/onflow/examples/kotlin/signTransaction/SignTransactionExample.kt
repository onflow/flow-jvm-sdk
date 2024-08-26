package org.onflow.examples.kotlin.signTransaction

import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.examples.kotlin.ExamplesUtils
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.crypto.Crypto.getSigner
import org.onflow.flow.sdk.crypto.PrivateKey

internal class SignTransactionExample(
    privateKey: PrivateKey,
    accessApiConnection: FlowAccessApi
) {
    private val privateKey = privateKey
    private val accessAPI = accessApiConnection

    private val connector = AccessAPIConnector(privateKey, accessAPI)

    fun singlePartySingleSignature(
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

        val signer = getSigner(privateKey, payerAccountKey.hashAlgo)
        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey.id, signer)

        return getFlowTransactionResult(tx)
    }

    fun singlePartyMultiSignature(
        payerAddress: FlowAddress,
        scriptName: String = "cadence/simple_transaction.cdc",
        gasLimit: Long = 500
    ): FlowTransactionResult {
        val payerAccountKey = connector.getAccountKey(payerAddress, 0)

        val payerAccountKey2 = connector.getAccountKey(payerAddress, 1)

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

        val signer = getSigner(privateKey, payerAccountKey.hashAlgo)

        // account 1 signs the envelope with key 1
        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey.id, signer)

        // account 1 signs the envelope with key 2
        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey2.id, signer)

        return getFlowTransactionResult(tx)
    }



    private fun getFlowTransactionResult(tx: FlowTransaction): FlowTransactionResult {
        val txID = when (val response = accessAPI.sendTransaction(tx)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
        return connector.waitForSeal(txID)
    }
}
