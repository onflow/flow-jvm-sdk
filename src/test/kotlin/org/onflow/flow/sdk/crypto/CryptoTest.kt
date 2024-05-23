package org.onflow.flow.sdk.crypto

import it.unisa.dia.gas.crypto.jpbc.signature.bls01.generators.BLS01KeyPairGenerator
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.generators.BLS01ParametersGenerator
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01KeyGenerationParameters
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01Parameters
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01PrivateKeyParameters
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01PublicKeyParameters
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.params.AsymmetricKeyParameter
import org.onflow.flow.sdk.HashAlgorithm
import org.bouncycastle.util.encoders.Hex
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.onflow.flow.sdk.SignatureAlgorithm
import org.onflow.flow.sdk.crypto.Crypto.encodeBLSPublicKey
import org.onflow.flow.sdk.crypto.Crypto.serializeBLSPublicKey
import org.onflow.flow.sdk.crypto.Crypto.setupBLSParameters
import java.math.BigInteger
import java.security.SecureRandom
import java.security.Signature

internal class CryptoTest {

    private fun generateBLSKeyPair(): AsymmetricCipherKeyPair {
        val parameters = setupBLSParameters()
        val keyPairGenerator = BLS01KeyPairGenerator()
        keyPairGenerator.init(BLS01KeyGenerationParameters(SecureRandom(), parameters))
        return keyPairGenerator.generateKeyPair()
    }

    @Test
    fun `Can generate KeyPair`() {
        val ecdsaKeyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        assertNotNull(ecdsaKeyPair.private)
        assertNotNull(ecdsaKeyPair.public)

        val blsKeyPair = Crypto.generateKeyPair(SignatureAlgorithm.BLS_BLS12_381)
        assertNotNull(blsKeyPair.private)
        assertNotNull(blsKeyPair.public)
    }

    @Test
    fun `Test generating different key pairs`() {
        val ecdsaKeyPair1 = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        val ecdsaKeyPair2 = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        assertNotEquals(ecdsaKeyPair1.private, ecdsaKeyPair2.private)
        assertNotEquals(ecdsaKeyPair1.public, ecdsaKeyPair2.public)

        val blsKeyPair1 = Crypto.generateKeyPair(SignatureAlgorithm.BLS_BLS12_381)
        val blsKeyPair2 = Crypto.generateKeyPair(SignatureAlgorithm.BLS_BLS12_381)
        assertNotEquals(blsKeyPair1.private, blsKeyPair2.private)
        assertNotEquals(blsKeyPair1.public, blsKeyPair2.public)
    }

    @Test
    fun `Can encode and decode BLS private key`() {
        // Generate a BLS private key
        val keyPair = generateBLSKeyPair()
        val privateKey = keyPair.private as BLS01PrivateKeyParameters

        // Serialize the BLS private key to a byte array
        val serializedKey: ByteArray = Crypto.serializeBLSPrivateKey(privateKey)
        assertNotNull(serializedKey, "Serialized key should not be null")
        assertTrue(serializedKey.isNotEmpty(), "Serialized key should not be empty")

        // Decode the serialized BLS private key
        val decodedKey: AsymmetricKeyParameter = Crypto.encodeBLSPrivateKey(serializedKey)
        assertNotNull(decodedKey, "Decoded key should not be null")

        // Verify that the decoded key matches the original key
        val reserializedKey: ByteArray = Crypto.serializeBLSPrivateKey(decodedKey as BLS01PrivateKeyParameters)
        assertNotNull(reserializedKey, "Reserialized key should not be null")
        assertTrue(reserializedKey.isNotEmpty(), "Reserialized key should not be empty")
        assertArrayEquals(serializedKey, reserializedKey, "Reserialized key should match the original serialized key")
    }

    @Test
    fun `Can decode private key`() {
        val ecdsaKeyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        val decodedEcdsaPrivateKey = Crypto.decodePrivateKey(ecdsaKeyPair.private.hex, SignatureAlgorithm.ECDSA_P256)
        assertNotNull(decodedEcdsaPrivateKey)
        assertEquals(ecdsaKeyPair.private.hex, decodedEcdsaPrivateKey.hex)

        val blsKeyPair = Crypto.generateKeyPair(SignatureAlgorithm.BLS_BLS12_381)
        val decodedBlsPrivateKey = Crypto.decodePrivateKey(blsKeyPair.private.hex, SignatureAlgorithm.BLS_BLS12_381)
        assertNotNull(decodedBlsPrivateKey)
        assertEquals(blsKeyPair.private.hex, decodedBlsPrivateKey.hex)
    }

    @Test
    fun `Private key throws exception when invalid`() {
        assertThrows(IllegalArgumentException::class.java) {
            Crypto.decodePrivateKey("invalidKey", SignatureAlgorithm.ECDSA_P256)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Crypto.decodePrivateKey("invalidKey", SignatureAlgorithm.BLS_BLS12_381)
        }
    }

    @Test
    fun `Can encode and decode BLS public key`() {
        // Generate BLS key pair
        val keyPair = generateBLSKeyPair()
        val publicKey = keyPair.public as BLS01PublicKeyParameters

        // Serialize the BLS public key to a byte array
        val serializedKey: ByteArray = serializeBLSPublicKey(publicKey)
        assertNotNull(serializedKey, "Serialized key should not be null")
        assertTrue(serializedKey.isNotEmpty(), "Serialized key should not be empty")

        // Decode the serialized BLS public key
        val decodedKey: AsymmetricKeyParameter = encodeBLSPublicKey(serializedKey)
        assertNotNull(decodedKey, "Decoded key should not be null")

        // Verify that the decoded key matches the original key
        val reserializedKey: ByteArray = serializeBLSPublicKey(decodedKey as BLS01PublicKeyParameters)
        assertNotNull(reserializedKey, "Reserialized key should not be null")
        assertTrue(reserializedKey.isNotEmpty(), "Reserialized key should not be empty")
        assertArrayEquals(serializedKey, reserializedKey, "Reserialized key should match the original serialized key")
    }

    @Test
    fun `Can decode public key`() {
        val ecdsaKeyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)
        val decodedEcdsaPublicKey = Crypto.decodePublicKey(ecdsaKeyPair.public.hex, SignatureAlgorithm.ECDSA_P256)
        assertNotNull(decodedEcdsaPublicKey)
        assertEquals(ecdsaKeyPair.public.hex, decodedEcdsaPublicKey.hex)

        val blsKeyPair = Crypto.generateKeyPair(SignatureAlgorithm.BLS_BLS12_381)
        val decodedBlsPublicKey = Crypto.decodePublicKey(blsKeyPair.public.hex, SignatureAlgorithm.BLS_BLS12_381)
        assertNotNull(decodedBlsPublicKey)
        assertEquals(blsKeyPair.public.hex, decodedBlsPublicKey.hex)
    }

    @Test
    fun `Public key throws exception when invalid`() {
        assertThrows(IllegalArgumentException::class.java) {
            Crypto.decodePublicKey("invalidKey", SignatureAlgorithm.ECDSA_P256)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Crypto.decodePublicKey("invalidKey", SignatureAlgorithm.BLS_BLS12_381)
        }
    }

//    @Test
//    fun `Get signer`() {
//        val keyPair = Crypto.generateKeyPair()
//        val signer = Crypto.getSigner(keyPair.private)
//        assertNotNull(signer)
//    }
//    @Test
//    fun `Get hasher`() {
//        val hasher = Crypto.getHasher()
//        assertNotNull(hasher)
//    }
//
//    @Test
//    fun `Test normalizeSignature`() {
//        val keyPair = Crypto.generateKeyPair()
//
//        val ecdsaSign = Signature.getInstance(HashAlgorithm.SHA3_256.id)
//        ecdsaSign.initSign(keyPair.private.key)
//        ecdsaSign.update("test".toByteArray())
//
//        val signature = ecdsaSign.sign()
//
//        val normalizedSignature = Crypto.normalizeSignature(signature, keyPair.private.ecCoupleComponentSize)
//
//        val expectedLength = 2 * keyPair.private.ecCoupleComponentSize
//        assertEquals(expectedLength, normalizedSignature.size)
//    }
//
//    @Test
//    fun `Test extractRS`() {
//        val keyPair = Crypto.generateKeyPair()
//
//        val ecdsaSign = Signature.getInstance(HashAlgorithm.SHA3_256.id)
//        ecdsaSign.initSign(keyPair.private.key)
//
//        println(keyPair.private.key)
//        ecdsaSign.update("test".toByteArray())
//
//        val signature = ecdsaSign.sign()
//
//        val (r, s) = Crypto.extractRS(signature)
//
//        assertTrue(r > BigInteger.ZERO)
//        assertTrue(s > BigInteger.ZERO)
//    }
//
//    @Test
//    fun `Hasher implementation`() {
//        val hasher = HasherImpl(HashAlgorithm.SHA3_256)
//        val hashedBytes = hasher.hash("test".toByteArray())
//        assertNotNull(hashedBytes)
//    }
//
//    @Test
//    fun `Signer implementation`() {
//        val keyPair = Crypto.generateKeyPair()
//        val signer = SignerImpl(keyPair.private, HashAlgorithm.SHA3_256)
//        val signature = signer.sign("test".toByteArray())
//        assertNotNull(signature)
//    }
}
