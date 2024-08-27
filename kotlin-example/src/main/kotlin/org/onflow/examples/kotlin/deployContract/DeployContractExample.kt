package org.onflow.examples.kotlin.deployContract

import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.examples.kotlin.ExamplesUtils
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.Field
import org.onflow.flow.sdk.cadence.StringField
import org.onflow.flow.sdk.cadence.UFix64NumberField
import org.onflow.flow.sdk.cadence.UInt8NumberField
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.crypto.PrivateKey

internal class DeployContractExample(
    privateKey: PrivateKey,
    accessApiConnection: FlowAccessApi
) {
    private val privateKey = privateKey
    private val accessAPI = accessApiConnection

    private val connector = AccessAPIConnector(privateKey, accessAPI)

    fun deployContract(
        payerAddress: FlowAddress,
        contractName: String = "GreatToken",
        scriptName: String = "cadence/great_token.cdc",
        gasLimit: Long = 1000L,
    ): FlowTransactionResult {
        val payerAccountKey = connector.getAccountKey(payerAddress, 0)
        val signer = Crypto.getSigner(privateKey, payerAccountKey.hashAlgo)

        val contractCode = ExamplesUtils.loadScript(scriptName)
        val contractScript =  """
                transaction() {
                    prepare(signer: &Account) {
                        signer.contracts.add(
                            name: "$contractName", code: "$contractCode".utf8
                        )
                    }
                }
            """

        var tx = FlowTransaction(
            script = FlowScript(
                contractScript),
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
