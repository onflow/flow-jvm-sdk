package org.onflow.flow.sdk.crypto

import org.onflow.flow.sdk.HashAlgorithm
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.onflow.flow.sdk.SignatureAlgorithm
import java.math.BigInteger
import java.security.Signature

internal class CryptoTest {
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
