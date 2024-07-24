package org.onflow.flow.sdk.crypto

import org.onflow.flow.sdk.HashAlgorithm
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.security.Signature

internal class SignTest {
    @Test
    fun `Can generate KeyPair`() {
        val keyPair = Crypto.generateKeyPair()
        assertNotNull(keyPair.private)
        assertNotNull(keyPair.public)
    }

    @Test
    fun `Test generating different key pairs`() {
        val keyPair1 = Crypto.generateKeyPair()
        val keyPair2 = Crypto.generateKeyPair()

        assertNotEquals(keyPair1.private, keyPair2.private)
        assertNotEquals(keyPair1.public, keyPair2.public)
    }

    @Test
    fun `Can decode private key`() {
        val keyPair = Crypto.generateKeyPair()
        val decodedPrivateKey = Crypto.decodePrivateKey(keyPair.private.hex)
        assertNotNull(decodedPrivateKey)
        assertEquals(keyPair.private.hex, decodedPrivateKey.hex)
    }

    @Test
    fun `Private key throws exception when invalid`() {
        assertThrows(IllegalArgumentException::class.java) {
            Crypto.decodePrivateKey("invalidKey")
        }
    }

    @Test
    fun `Can decode public key`() {
        val keyPair = Crypto.generateKeyPair()
        val decodedPublicKey = Crypto.decodePublicKey(keyPair.public.hex)
        assertNotNull(decodedPublicKey)
        assertEquals(keyPair.public.hex, decodedPublicKey.hex)
    }

    @Test
    fun `Public key throws exception when invalid`() {
        assertThrows(IllegalArgumentException::class.java) {
            Crypto.decodePublicKey("invalidKey")
        }
    }

    @Test
    fun `Get signer`() {
        val keyPair = Crypto.generateKeyPair()
        val signer = Crypto.getSigner(keyPair.private)
        assertNotNull(signer)
    }

    @Test
    fun `Test formatSignature`() {
        val keyPair = Crypto.generateKeyPair()

        val ecdsaSign = Signature.getInstance("SHA3-256withECDSA")
        ecdsaSign.initSign(keyPair.private.key)
        ecdsaSign.update("test".toByteArray())

        val signature = ecdsaSign.sign()

        val curveOrderSize = Crypto.getCurveOrderSize(keyPair.private.curve)
        val normalizedSignature = Crypto.formatSignature(signature, curveOrderSize)

        val expectedLength = 2 * curveOrderSize
        assertEquals(expectedLength, normalizedSignature.size)
    }

    @Test
    fun `Test extractRS`() {
        val keyPair = Crypto.generateKeyPair()

        val ecdsaSign = Signature.getInstance("SHA3-256withECDSA")
        ecdsaSign.initSign(keyPair.private.key)
        ecdsaSign.update("test".toByteArray())

        val signature = ecdsaSign.sign()

        val (r, s) = Crypto.extractRS(signature)

        assertTrue(r > BigInteger.ZERO)
        assertTrue(s > BigInteger.ZERO)
    }

    @Test
    fun `Signer implementation for SHA3_256`() {
        val keyPair = Crypto.generateKeyPair()
        val signer = SignerImpl(keyPair.private, HashAlgorithm.SHA3_256)
        val signature = signer.sign("test".toByteArray())
        assertNotNull(signature)
    }

    @Test
    fun `Signer implementation for SHA2_256`() {
        val keyPair = Crypto.generateKeyPair()
        val signer = SignerImpl(keyPair.private, HashAlgorithm.SHA2_256)
        val signature = signer.sign("test".toByteArray())
        assertNotNull(signature)
    }

    @Test
    fun `Signer implementation for Keccak-256`() {
        val keyPair = Crypto.generateKeyPair()
        val signer = SignerImpl(keyPair.private, HashAlgorithm.KECCAK256)
        val signature = signer.sign("test".toByteArray())
        assertNotNull(signature)
    }

    @Test
    fun `Signer implementation for KMAC128 throws exception`() {
        val keyPair = Crypto.generateKeyPair()
        val key = "thisKeyIsAtLeast16Bytes".toByteArray()
        val hasher = HasherImpl(HashAlgorithm.KMAC128, key)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            SignerImpl(keyPair.private, HashAlgorithm.KMAC128, hasher).sign("test".toByteArray())
        }
        assertEquals("Unsupported hash algorithm: KMAC128", exception.message)
    }
}
