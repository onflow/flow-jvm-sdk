package com.nftco.flow.sdk.crypto

import com.nftco.flow.sdk.HashAlgorithm
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class CryptoTest {

    @Test
    fun `Can generate KeyPair`() {
        val keyPair = Crypto.generateKeyPair()
        assertNotNull(keyPair.private)
        assertNotNull(keyPair.public)
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
    fun `Get hasher`() {
        val hasher = Crypto.getHasher()
        assertNotNull(hasher)
    }

    @Test
    fun `Normalize signature`() {
        val keyPair = Crypto.generateKeyPair()
        val data = "test".toByteArray()

        val originalSignature = Crypto.getSigner(keyPair.private).sign(data)
        val normalizedSignature = Crypto.normalizeSignature(originalSignature, keyPair.private.ecCoupleComponentSize)

        assertEquals(2 * keyPair.private.ecCoupleComponentSize, normalizedSignature.size)
    }

    @Test
    fun `Extract RS from valid signature`() {
        val keyPair = Crypto.generateKeyPair()
        val signer = SignerImpl(keyPair.private, HashAlgorithm.SHA3_256)
        val signature = signer.sign("test".toByteArray())

        val (r, s) = Crypto.extractRS(signature)
        println(r)
        println(s)

        // Assert
    }

    @Test
    fun `Hasher implementation`() {
        val hasher = HasherImpl(HashAlgorithm.SHA3_256)
        val hashedBytes = hasher.hash("test".toByteArray())
        assertNotNull(hashedBytes)
    }

    @Test
    fun `Signer implementation`() {
        val keyPair = Crypto.generateKeyPair()
        val signer = SignerImpl(keyPair.private, HashAlgorithm.SHA3_256)
        val signature = signer.sign("test".toByteArray())
        assertNotNull(signature)
    }

    // test exception handling on sign()

}
