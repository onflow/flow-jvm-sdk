package org.onflow.examples.kotlin.verifySignature.userSignatureValidateAll

import org.onflow.examples.kotlin.ExamplesUtils
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.*
import org.onflow.flow.sdk.crypto.Crypto

internal class UserSignatureValidateAllExample(
    private val accessAPI: FlowAccessApi
) {
    fun verifyUserSignatureValidateAll(
        aliceAddress: FlowAddress,
        bobAddress: FlowAddress
    ): Field<*> {
        // Create the keys
        val keyPairAlice =  Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        val privateKeyAlice = keyPairAlice.private
        val publicKeyAlice = keyPairAlice.public

        val keyPairBob =  Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        val privateKeyBob = keyPairBob.private
        val publicKeyBob = keyPairBob.public

        // Create the message that will be signed (In this case, it's "ananas")
        val message = "ananas".toByteArray()

        // Convert the message to an unsigned byte array
        val unsignedMessage = message.map { (it.toInt() and 0xFF).toByte() }.toByteArray()

        val signerAlice = Crypto.getSigner(privateKeyAlice, HashAlgorithm.SHA3_256)
        val signerBob = Crypto.getSigner(privateKeyBob, HashAlgorithm.SHA3_256)

        // Sign the message with Alice and Bob
        val signatureAlice = signerAlice.sign(unsignedMessage)
        val signatureBob = signerBob.sign(unsignedMessage)

        // The signature indexes correspond to the key indexes on the address
        val keyIndexes = ArrayField(listOf(IntNumberField("1"), IntNumberField("0")))

        // Prepare the Cadence arguments
        val signatures = ArrayField(listOf(
            StringField(signatureBob.toHexString()),
            StringField(signatureAlice.toHexString())
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

        println("Unsigned Message Hex (Kotlin): ${unsignedMessage.joinToString("") { "%02x".format(it) }}")
        println("Signature Alice (Hex): ${signatureAlice.toHexString()}")
        println("Signature Bob (Hex): ${signatureBob.toHexString()}")
        println("Public Key Alice (Hex): ${publicKeyAlice.hex}")
        println("Public Key Bob (Hex): ${publicKeyBob.hex}")
        println("Key Indexes: $keyIndexes")

        return result.jsonCadence
    }

    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
}
