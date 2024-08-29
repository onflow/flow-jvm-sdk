package org.onflow.examples.kotlin.verifySignature.userSignatureValidateAny

import org.onflow.examples.kotlin.ExamplesUtils.toHexString
import org.onflow.examples.kotlin.ExamplesUtils.loadScriptContent
import org.onflow.examples.kotlin.ExamplesUtils.toUnsignedByteArray
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.*
import org.onflow.flow.sdk.crypto.Crypto
import org.onflow.flow.sdk.crypto.PrivateKey

internal class UserSignatureValidateAnyExample(
    private val accessAPI: FlowAccessApi
) {
    fun verifyUserSignatureValidateAny(
        aliceAddress: FlowAddress,
        alicePrivateKey: PrivateKey,
        message: String
    ): Field<*> {
        // Convert the message to an unsigned byte array
        val messageBytes = (message.toByteArray()).toUnsignedByteArray()

        // Sign the message with Alice's key
        val signerAlice = Crypto.getSigner(alicePrivateKey, HashAlgorithm.SHA3_256)
        val signatureAlice = signerAlice.sign(messageBytes)
        val signatureAliceHex = signatureAlice.toHexString()

        // Execute the script to verify the signature on-chain
        val result = when (
            val response = accessAPI.simpleFlowScript {
                script {
                    loadScriptContent("cadence/user_signature_validate_any.cdc")
                }
                arguments {
                    listOf(
                        AddressField(aliceAddress.bytes),
                        StringField(signatureAliceHex),
                        StringField(message)
                    )
                }
            }
        ) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

        return result.jsonCadence as BooleanField
    }
}
