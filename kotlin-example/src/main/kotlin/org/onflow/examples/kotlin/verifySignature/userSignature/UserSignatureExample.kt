package org.onflow.examples.kotlin.verifySignature.userSignature

import org.onflow.examples.kotlin.ExamplesUtils.toHexString
import org.onflow.examples.kotlin.ExamplesUtils.loadScriptContent
import org.onflow.examples.kotlin.ExamplesUtils.toUnsignedByteArray
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.*
import org.onflow.flow.sdk.crypto.Crypto
import java.math.BigInteger

internal class UserSignatureExample(
    private val accessAPI: FlowAccessApi
) {
    fun verifyUserSignature(
        aliceAddress: FlowAddress,
        bobAddress: FlowAddress
    ): Field<*> {
        // Create the keys
        val keyPairAlice = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        val privateKeyAlice = keyPairAlice.private
        val publicKeyAlice = keyPairAlice.public

        val keyPairBob = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        val privateKeyBob = keyPairBob.private
        val publicKeyBob = keyPairBob.public

        // Create the message that will be signed
        val amount = UFix64NumberField("100.00")
        val amountBigEndianBytes = toBigEndianBytes(amount.value!!)

        val message = (aliceAddress.bytes + bobAddress.bytes + amountBigEndianBytes).toUnsignedByteArray()

        val signerAlice = Crypto.getSigner(privateKeyAlice, HashAlgorithm.SHA3_256)
        val signerBob = Crypto.getSigner(privateKeyBob, HashAlgorithm.SHA3_256)

        // Sign the message with Alice and Bob
        val signatureAlice = signerAlice.sign(message)
        val signatureBob = signerBob.sign(message)

        // Each signature has half weight
        val weightAlice = UFix64NumberField("0.5")
        val weightBob = UFix64NumberField("0.5")

        // Call the script to verify the signatures on-chain
        val result = when (
            val response = accessAPI.simpleFlowScript {
                script {
                    loadScriptContent("cadence/user_signature.cdc")
                }
                arguments {
                    listOf(
                        ArrayField( // public keys
                            listOf(
                                StringField(publicKeyAlice.hex),
                                StringField(publicKeyBob.hex)
                            )
                        ),
                        ArrayField( // weights
                            listOf(
                                weightAlice,
                                weightBob
                            )
                        ),
                        ArrayField(
                            listOf(
                                StringField(signatureAlice.toHexString()),
                                StringField(signatureBob.toHexString())
                            )
                        ),
                        AddressField(aliceAddress.bytes), AddressField(bobAddress.bytes),
                        UFix64NumberField("100.00")
                    )
                }
            }
        ) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

        return result.jsonCadence
    }

    private fun toBigEndianBytes(value: String): ByteArray {
        // Convert UFix64 string to BigInteger by multiplying by 10^8
        val ufix64Value = BigInteger(value.replace(".", "")) * BigInteger.TEN.pow(8 - value.split(".")[1].length)

        // Convert BigInteger to byte array in big-endian order
        return ufix64Value.toByteArray().let {
            if (it.size < 8) {
                // Add leading zeroes if necessary to make it 8 bytes long
                ByteArray(8 - it.size) + it
            } else {
                it
            }
        }
    }
}
