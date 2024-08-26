package org.onflow.examples.kotlin.addKey

import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.crypto.PrivateKey
import org.onflow.flow.sdk.crypto.PublicKey

internal class AddAccountKeyExample(
    privateKey: PrivateKey,
    accessApiConnection: FlowAccessApi
) {
    private val privateKey = privateKey
    private val accessAPI = accessApiConnection

    private val connector = AccessAPIConnector(privateKey, accessAPI)

    fun addKeyToAccount(payerAddress: FlowAddress, publicKey: PublicKey): FlowTransactionResult {
    }

}
