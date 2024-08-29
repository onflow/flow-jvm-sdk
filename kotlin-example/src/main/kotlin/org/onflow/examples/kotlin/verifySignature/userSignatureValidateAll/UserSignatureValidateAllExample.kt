package org.onflow.examples.kotlin.verifySignature.userSignatureValidateAll

import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.examples.kotlin.ExamplesUtils
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.*
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.crypto.PrivateKey

internal class UserSignatureValidateAllExample(
    private val accessAPI: FlowAccessApi
) {
    fun verifyUserSignatureValidateAll(
        aliceAddress: FlowAddress,
        alicePrivateKey: PrivateKey
    ): Field<*> {

        val account = AccessAPIConnector(alicePrivateKey, accessAPI).getAccount(aliceAddress)
        val aliceKey1 = account.keys[0]
        val aliceKey2 = account.keys[1]

        val message = "ananas".toByteArray()
        val unsignedMessage = message.map { (it.toInt() and 0xFF).toByte() }.toByteArray()

        val signerAlice1 = Crypto.getSigner(alicePrivateKey, HashAlgorithm.SHA3_256)
        val signerAlice2 = Crypto.getSigner(alicePrivateKey, HashAlgorithm.SHA3_256)

        val signatureAlice1 = signerAlice1.sign(unsignedMessage)
        val signatureAlice2 = signerAlice2.sign(unsignedMessage)

        // The signature indexes correspond to the key indexes on the address
        val keyIndexes = ArrayField(listOf(IntNumberField("0"), IntNumberField("1")))

        // Prepare the Cadence arguments
        val signatures = ArrayField(listOf(
            StringField(signatureAlice1.toHexString()),
            StringField(signatureAlice2.toHexString())
        ))

        // Call the script to verify the signatures on-chain
        val result = when (val response = accessAPI.simpleFlowScript {
            script {
                ExamplesUtils.loadScriptContent("cadence/user_signature_validate_all.cdc")
            }
            arguments {
                listOf(
                    AddressField(aliceAddress.bytes),  // Assuming Alice's address is used here
                    signatures,
                    keyIndexes,
                    StringField(message.toString(Charsets.UTF_8))  // The message is passed as a UTF-8 string
                )
            }
        }) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
        return result.jsonCadence
    }

    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
}
