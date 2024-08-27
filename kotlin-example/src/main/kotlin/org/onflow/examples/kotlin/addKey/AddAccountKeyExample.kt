package org.onflow.examples.kotlin.addKey

import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.examples.kotlin.ExamplesUtils
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.StringField
import org.onflow.flow.sdk.cadence.UFix64NumberField
import org.onflow.flow.sdk.cadence.UInt8NumberField
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.crypto.PrivateKey

internal class AddAccountKeyExample(
    privateKey: PrivateKey,
    accessApiConnection: FlowAccessApi
) {
    private val privateKey = privateKey
    private val accessAPI = accessApiConnection

    private val connector = AccessAPIConnector(privateKey, accessAPI)

    fun addKeyToAccount(
        payerAddress: FlowAddress,
        scriptName: String = "cadence/add_key.cdc",
        gasLimit: Long = 500
    ): FlowTransactionResult {
        val payerAccountKey = connector.getAccountKey(payerAddress, 0)


        var tx = FlowTransaction(
            script = FlowScript(ExamplesUtils.loadScript(scriptName)),
            arguments = listOf(
                FlowArgument(StringField(payerAccountKey.publicKey.base16Value)),
                FlowArgument(UInt8NumberField(payerAccountKey.signAlgo.index.toString())),
                FlowArgument(UInt8NumberField(payerAccountKey.hashAlgo.index.toString())),
                FlowArgument(UFix64NumberField("1000.0"))
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
