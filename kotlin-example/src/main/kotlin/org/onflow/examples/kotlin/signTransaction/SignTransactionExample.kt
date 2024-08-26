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
        gasLimit: Long = 1000
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

        // Account 1 signs the envelope with key 1
        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey.id, signer)

        // Account 1 signs the envelope with key 2
        tx = tx.addEnvelopeSignature(payerAddress, payerAccountKey2.id, signer)

        return getFlowTransactionResult(tx)
    }

    fun multiPartySingleSignature(
        account1PrivateKey: PrivateKey,
        account2PrivateKey: PrivateKey,
        payerAddress: FlowAddress,
        authorizerAddress: FlowAddress,
        scriptName: String = "cadence/simple_transaction.cdc",
        gasLimit: Long = 500
    ): FlowTransactionResult {
        val account1Key = connector.getAccountKey(authorizerAddress, 0)
        val account2Key = connector.getAccountKey(payerAddress, 0)

        var tx = FlowTransaction(
            script = FlowScript(ExamplesUtils.loadScript(scriptName)),
            arguments = listOf(),
            referenceBlockId = connector.latestBlockID,
            gasLimit = gasLimit,
            proposalKey = FlowTransactionProposalKey(
                address = authorizerAddress,
                keyIndex = account1Key.id,
                sequenceNumber = account1Key.sequenceNumber.toLong()
            ),
            payerAddress = payerAddress,
            authorizers = listOf(authorizerAddress)
        )

        // Account 1 signs the payload with key 1
        val account1Signer = getSigner(account1PrivateKey, account1Key.hashAlgo)
        tx = tx.addPayloadSignature(authorizerAddress, account1Key.id, account1Signer)

        // Account 2 signs the envelope with key 2
        val account2Signer = getSigner(account2PrivateKey, account2Key.hashAlgo)
        tx = tx.addEnvelopeSignature(payerAddress, account2Key.id, account2Signer)

        // Send the transaction and wait for the result
        return getFlowTransactionResult(tx)
    }

    fun multiPartyMultiSignature(
        account1PrivateKeys: List<PrivateKey>,
        account2PrivateKeys: List<PrivateKey>,
        payerAddress: FlowAddress,
        authorizerAddress: FlowAddress,
        scriptName: String = "cadence/simple_transaction.cdc",
        gasLimit: Long = 500
    ): FlowTransactionResult {
        val account1Key1 = connector.getAccountKey(authorizerAddress, 0)
        val account1Key2 = connector.getAccountKey(authorizerAddress, 1)
        val account2Key1 = connector.getAccountKey(payerAddress, 0)
        val account2Key2 = connector.getAccountKey(payerAddress, 1)

        var tx = FlowTransaction(
            script = FlowScript(ExamplesUtils.loadScript(scriptName)),
            arguments = listOf(),
            referenceBlockId = connector.latestBlockID,
            gasLimit = gasLimit,
            proposalKey = FlowTransactionProposalKey(
                address = authorizerAddress,
                keyIndex = account1Key1.id,
                sequenceNumber = account1Key1.sequenceNumber.toLong()
            ),
            payerAddress = payerAddress,
            authorizers = listOf(authorizerAddress)
        )

        // Account 1 signs the payload with key 1
        val account1Signer1 = getSigner(account1PrivateKeys[0], account1Key1.hashAlgo)
        tx = tx.addPayloadSignature(authorizerAddress, account1Key1.id, account1Signer1)

        // Account 1 signs the payload with key 2
        val account1Signer2 = getSigner(account1PrivateKeys[1], account1Key2.hashAlgo)
        tx = tx.addPayloadSignature(authorizerAddress, account1Key2.id, account1Signer2)

        // Account 2 signs the envelope with key 1
        val account2Signer1 = getSigner(account2PrivateKeys[0], account2Key1.hashAlgo)
        tx = tx.addEnvelopeSignature(payerAddress, account2Key1.id, account2Signer1)

        // Account 2 signs the envelope with key 2
        val account2Signer2 = getSigner(account2PrivateKeys[1], account2Key2.hashAlgo)
        tx = tx.addEnvelopeSignature(payerAddress, account2Key2.id, account2Signer2)

        return getFlowTransactionResult(tx)
    }

    fun multiParty2Authorizers(
        account1PrivateKey: PrivateKey,
        account2PrivateKey: PrivateKey,
        payerAddress: FlowAddress,
        authorizer1Address: FlowAddress,
        authorizer2Address: FlowAddress,
        scriptName: String = "cadence/simple_transaction_2_authorizers.cdc",
        gasLimit: Long = 500
    ): FlowTransactionResult {
        val account1Key = connector.getAccountKey(authorizer1Address, 0)
        val account2Key = connector.getAccountKey(authorizer2Address, 0)

        var tx = FlowTransaction(
            script = FlowScript(ExamplesUtils.loadScript(scriptName)),
            arguments = listOf(),
            referenceBlockId = connector.latestBlockID,
            gasLimit = gasLimit,
            proposalKey = FlowTransactionProposalKey(
                address = authorizer1Address,
                keyIndex = account1Key.id,
                sequenceNumber = account1Key.sequenceNumber.toLong()
            ),
            payerAddress = payerAddress,
            authorizers = listOf(authorizer1Address, authorizer2Address)
        )

        // Account 1 signs the payload with key 1
        val account1Signer = getSigner(account1PrivateKey, account1Key.hashAlgo)
        tx = tx.addPayloadSignature(authorizer1Address, account1Key.id, account1Signer)

        // Account 2 signs the envelope with key 2
        val account2Signer = getSigner(account2PrivateKey, account2Key.hashAlgo)
        tx = tx.addEnvelopeSignature(authorizer2Address, account2Key.id, account2Signer)

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
