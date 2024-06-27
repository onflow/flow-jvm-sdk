package org.onflow.flow.sdk.crypto

import org.onflow.flow.sdk.HashAlgorithm
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    fun `Get hasher`() {
        val hasher = Crypto.getHasher()
        assertNotNull(hasher)
    }

    @Test
    fun `Test normalizeSignature`() {
        val keyPair = Crypto.generateKeyPair()

        val ecdsaSign = Signature.getInstance(HashAlgorithm.SHA3_256.id)
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

        val ecdsaSign = Signature.getInstance(HashAlgorithm.SHA3_256.id)
        ecdsaSign.initSign(keyPair.private.key)

        ecdsaSign.update("test".toByteArray())

        val signature = ecdsaSign.sign()

        val (r, s) = Crypto.extractRS(signature)

        assertTrue(r > BigInteger.ZERO)
        assertTrue(s > BigInteger.ZERO)
    }

    @Test
    fun `Hasher implementation`() {
        val hasher = HasherImpl(HashAlgorithm.SHA3_256)
        val hashedBytes = hasher.hash("test".toByteArray())
        assertNotNull(hashedBytes)
    }

    @Test
    fun `Hasher implementation for Keccak-256`() {
        val hasher = HasherImpl(HashAlgorithm.KECCAK256)
        val hashedBytes = hasher.hash("test".toByteArray())
        assertNotNull(hashedBytes)
    }

    @Test
    fun `Hasher implementation for KMAC128`() {
        val key = "key".toByteArray()
        val hasher = HasherImpl(HashAlgorithm.KMAC128, key)
        val hashedBytes = hasher.hash("test".toByteArray())
        assertNotNull(hashedBytes)
    }

    @Test
    fun `Sanity check SHA3_256`() {
        val input = "test".toByteArray()
        val expected = hexStringToByteArray("36f028580bb02cc8272a9a020f4200e346e276ae664e45ee80745574e2f5ab80")

        val hasher = HasherImpl(HashAlgorithm.SHA3_256)
        val hash = hasher.hash(input)
        assertEquals(expected.toList(), hash.toList())
    }

    @Test
    fun `Sanity check SHA2_256`() {
        val input = "test".toByteArray()
        val expected = hexStringToByteArray("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")

        val hasher = HasherImpl(HashAlgorithm.SHA2_256)
        val hash = hasher.hash(input)
        assertEquals(expected.toList(), hash.toList())
    }

    @Test
    fun `Sanity check Keccak_256`() {
        val input = "test".toByteArray()
        val expected = hexStringToByteArray("9c22ff5f21f0b81b113e63f7db6da94fedef11b2119b4088b89664fb9a3cb658")

        val hasher = HasherImpl(HashAlgorithm.KECCAK256)
        val hash = hasher.hash(input)
        assertEquals(expected.toList(), hash.toList())
    }

    private fun ByteArray.toUnsignedIntList() = this.map { it.toInt() and 0xFF }

    @Test
    fun `Sanity check KMAC128`() {
        val input = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        val expected = listOf(
            hexStringToByteArray("e5780b0d3ea6f7d3a429c5706aa43a00fadb7d4d9628839e3187243f456ee14e"),
            hexStringToByteArray("3b1fba963cd8b0b59e8c1a6d71888b7143651af8ba0a7070c0979e2811324aa5")
        )
        val key = hexStringToByteArray("404142434445464748494a4b4c4d4e4f505152535455565758595a5b5c5d5e5f")
        val customizers = listOf(
           "".toByteArray(),
            "My Tagged Application".toByteArray()
        )
        val outputSize = 32

        // Test short key length
        val exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.KMAC128, key.sliceArray(0 until 15), customizers[0], outputSize).hash(input)
        }
        assertEquals("KMAC128 requires a key of at least 16 bytes", exception.message)

        customizers.forEachIndexed { index, customizer ->
            val hasher = HasherImpl(HashAlgorithm.KMAC128, key, customizer, outputSize)
            val hash = hasher.hash(input)

            // Detailed debug statements
            println("Customizer: ${customizer.decodeToString()}")
            println("Expected: ${expected[index].toUnsignedIntList()}")
            println("Actual  : ${hash.toUnsignedIntList()}")
            println("Key     : ${key.toUnsignedIntList()}")
            println("Input   : ${input.toUnsignedIntList()}")
            println("Customizer Bytes: ${customizer.toList()}")

            assertEquals(expected[index].toUnsignedIntList(), hash.toUnsignedIntList())
        }


    }

    private fun hexStringToByteArray(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
        }
        return data
    }

    @Test
    fun `Signer implementation`() {
        val keyPair = Crypto.generateKeyPair()
        val signer = SignerImpl(keyPair.private, HashAlgorithm.SHA3_256)
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
    fun `Signer implementation for KMAC128`() {
        val keyPair = Crypto.generateKeyPair()
        val key = "key".toByteArray()
        val hasher = HasherImpl(HashAlgorithm.KMAC128, key)
        val signer = SignerImpl(keyPair.private, HashAlgorithm.KMAC128, hasher)
        val signature = signer.sign("test".toByteArray())
        assertNotNull(signature)
    }
}
