package org.onflow.flow.sdk.crypto

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.security.Signature

internal class CryptoTest {
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
    fun `Test normalizeSignature`() {
        val keyPair = Crypto.generateKeyPair()

        val ecdsaSign = Signature.getInstance("SHA3-256withECDSA")
        ecdsaSign.initSign(keyPair.private.key)
        ecdsaSign.update("test".toByteArray())

        val signature = ecdsaSign.sign()

        val normalizedSignature = Crypto.normalizeSignature(signature, keyPair.private.ecCoupleComponentSize)

        val expectedLength = 2 * keyPair.private.ecCoupleComponentSize
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
}
