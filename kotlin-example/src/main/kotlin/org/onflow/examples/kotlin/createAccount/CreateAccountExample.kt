package org.onflow.examples.kotlin.createAccount

import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.examples.kotlin.ExamplesUtils.loadScript
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.AddressField
import org.onflow.flow.sdk.cadence.EventField
import org.onflow.flow.sdk.cadence.StringField
import org.onflow.flow.sdk.cadence.UInt8NumberField
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.crypto.PrivateKey
import org.onflow.flow.sdk.crypto.PublicKey

internal class CreateAccountExample(
    privateKey: PrivateKey,
    accessApiConnection: FlowAccessApi
) {
    private val privateKey = privateKey
    private val accessAPI = accessApiConnection

    private val connector = AccessAPIConnector(privateKey, accessAPI)

    fun createAccount(payerAddress: FlowAddress, publicKey: PublicKey): FlowAddress {
        val payerAccountKey = connector.getAccountKey(payerAddress, 0)

        var tx = FlowTransaction(
            script = FlowScript(loadScript("cadence/create_account.cdc")),
            arguments = listOf(
                FlowArgument(StringField(publicKey.hex)),
                FlowArgument(UInt8NumberField(publicKey.algo.index.toString()))
            ),
            referenceBlockId = connector.latestBlockID,
            gasLimit = 500,
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
        return getAccountCreatedAddress(txResult)
    }

    private fun getAccountCreatedAddress(txResult: FlowTransactionResult): FlowAddress {
        val address = txResult.events
            .find { it.type == "flow.AccountCreated" }
            ?.payload
            ?.let { (it.jsonCadence as EventField).value }
            ?.getRequiredField<AddressField>("address")
            ?.value as String

        return FlowAddress(address)
    }
}
