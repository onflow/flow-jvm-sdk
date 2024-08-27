package org.onflow.examples.kotlin.deployContract

import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.examples.kotlin.ExamplesUtils
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.Field
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
        gasLimit: Int = 1000,
        contractArgs: Map<String, Field<*>> = emptyMap()
    ): FlowTransactionStub {
        val contractCode = ExamplesUtils.loadScript("cadence/great_token.cdc")
        val payerAccountKey = connector.getAccountKey(payerAddress, 0)
        val signer = Crypto.getSigner(privateKey, payerAccountKey.hashAlgo)

        val sortedArgs = contractArgs.entries.sortedBy { it.key }
        val contractArgDeclarations = sortedArgs.joinToString(", ") { "${it.key}: ${it.value.type}" }
        val contractAddArgs = sortedArgs.joinToString(", ") { "${it.key}: ${it.key}" }

        return accessAPI.simpleFlowTransaction(
            address = payerAddress,
            signer = signer,
            keyIndex = payerAccountKey.id
        ) {
            script {
                """
                transaction($contractArgDeclarations) {
                    prepare(signer: &Account) {
                        signer.contracts.add(
                            name: "$contractName", code: "$contractCode".utf8$contractAddArgs
                        )
                    }
                }
            """
            }
            gasLimit(gasLimit)
            arguments {
                sortedArgs.forEach { arg { it.value } }
            }
        }
    }
}
