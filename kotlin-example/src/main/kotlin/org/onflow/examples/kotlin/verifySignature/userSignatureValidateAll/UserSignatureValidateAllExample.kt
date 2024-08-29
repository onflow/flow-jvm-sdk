package org.onflow.examples.kotlin.verifySignature.userSignatureValidateAll

import org.onflow.examples.kotlin.ExamplesUtils.toHexString
import org.onflow.examples.kotlin.ExamplesUtils.loadScriptContent
import org.onflow.examples.kotlin.ExamplesUtils.toUnsignedByteArray
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
        val message = ("ananas".toByteArray()).toUnsignedByteArray()

        val signerAlice1 = Crypto.getSigner(alicePrivateKey, HashAlgorithm.SHA3_256)
        val signerAlice2 = Crypto.getSigner(alicePrivateKey, HashAlgorithm.SHA3_256)

        val signatureAlice1 = signerAlice1.sign(message)
        val signatureAlice2 = signerAlice2.sign(message)

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
              loadScriptContent("cadence/user_signature_validate_all.cdc")
            }
            arguments {
                listOf(
                    AddressField(aliceAddress.bytes),
                    signatures,
                    keyIndexes,
                    StringField(message.toString(Charsets.UTF_8))
                )
            }
        }) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
        return result.jsonCadence
    }
}
