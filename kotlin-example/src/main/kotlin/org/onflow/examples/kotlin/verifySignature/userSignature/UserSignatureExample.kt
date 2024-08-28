package org.onflow.examples.kotlin.verifySignature.userSignature

import org.onflow.examples.kotlin.ExamplesUtils
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.*
import org.onflow.flow.sdk.crypto.Crypto
import java.math.BigInteger

internal class UserSignatureExample(
    private val accessAPI: FlowAccessApi
) {
    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
    private fun UFix64NumberField.toBigEndianBytes(): ByteArray {
        val parts = this.value!!.split(".")
        val integerPart = BigInteger(parts[0])
        val fractionalPart = BigInteger(parts.getOrElse(1) { "0" }.padEnd(8, '0'))

        val integerBytes = ByteArray(8)
        val fractionalBytes = ByteArray(8)

        val tempIntegerBytes = integerPart.toByteArray()
        val tempFractionalBytes = fractionalPart.toByteArray()

        // Copy integer part to the end of the 8-byte array
        System.arraycopy(tempIntegerBytes, 0, integerBytes, 8 - tempIntegerBytes.size, tempIntegerBytes.size)

        // Copy fractional part to the end of the 8-byte array
        System.arraycopy(tempFractionalBytes, 0, fractionalBytes, 8 - tempFractionalBytes.size, tempFractionalBytes.size)

        return integerBytes + fractionalBytes
    }

    fun runUserSignatureDemo(
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

        // Create the message that will be signed
        val amount = UFix64NumberField("100.00")

        println(amount.toBigEndianBytes().joinToString(", ") { it.toString() })

        val message = aliceAddress.bytes + bobAddress.bytes + amount.toBigEndianBytes()

        val messageAsHex = message.joinToString(", ") { it.toString() }
        println("Message bytes: [$messageAsHex]")

        val unsignedMessage = message.map { it.toInt() and 0xFF }
        val messageAsUnsignedHex = unsignedMessage.joinToString(", ") { it.toString() }
        println("Unsigned Message bytes: [$messageAsUnsignedHex]")

        val signerAlice = Crypto.getSigner(privateKeyAlice, HashAlgorithm.SHA3_256)
        val signerBob = Crypto.getSigner(privateKeyBob, HashAlgorithm.SHA3_256)

        // Sign the message with Alice and Bob
        val signatureAlice = signerAlice.sign(message)
        println(signatureAlice)
        val signatureBob = signerBob.sign(message)
        println(signatureBob)

        // Each signature has half weight
        val weightAlice = UFix64NumberField("0.5")
        val weightBob = UFix64NumberField("0.5")

        // Call the script to verify the signatures on-chain
        val result = when (val response = accessAPI.simpleFlowScript{
            script {
              ExamplesUtils.loadScriptContent("cadence/user_signature.cdc")
            }
            arguments {
                listOf(ArrayField( // public keys
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
                UFix64NumberField("100.00"))
            }
        }
        ) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

        return result.jsonCadence
    }
}


