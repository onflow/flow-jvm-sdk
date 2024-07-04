package org.onflow.flow.sdk.crypto

import org.bouncycastle.crypto.macs.KMAC
import org.bouncycastle.crypto.params.KeyParameter
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
        val key = "thisKeyIsAtLeast16Bytes".toByteArray()
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
    fun `Sanity check SHA3_384`() {
        val input = "test".toByteArray()
        val expected = hexStringToByteArray("e516dabb23b6e30026863543282780a3ae0dccf05551cf0295178d7ff0f1b41eecb9db3ff219007c4e097260d58621bd")

        val hasher = HasherImpl(HashAlgorithm.SHA3_384)
        val hash = hasher.hash(input)
        assertEquals(expected.toList(), hash.toList())
    }

    @Test
    fun `Sanity check SHA2_384`() {
        val input = "test".toByteArray()
        val expected = hexStringToByteArray("768412320f7b0aa5812fce428dc4706b3cae50e02a64caa16a782249bfe8efc4b7ef1ccb126255d196047dfedf17a0a9")

        val hasher = HasherImpl(HashAlgorithm.SHA2_384)
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

    @Test
    fun `Sanity check KMAC128`() {
        val input = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        val expected = listOf(
            hexStringToByteArray("E5780B0D3EA6F7D3A429C5706AA43A00FADBD7D49628839E3187243F456EE14E"),
            hexStringToByteArray("3B1FBA963CD8B0B59E8C1A6D71888B7143651AF8BA0A7070C0979E2811324AA5")
        )
        val key = hexStringToByteArray("404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F")
        val customizers = listOf(
            "".toByteArray(),
            "My Tagged Application".toByteArray()
        )
        val outputSize = 32

        // Test full input processing
        val hasher1 = HasherImpl(HashAlgorithm.KMAC128, key, customizers[0], outputSize)
        val hash1 = hasher1.hash(input)
        assertArrayEquals(expected[0], hash1)

        // Test incremental input processing
        val hasher2 = HasherImpl(HashAlgorithm.KMAC128, key, customizers[0], outputSize)
        hasher2.update(input, 0, 2)
        hasher2.update(input, 2, input.size - 2)
        val hash2 = hasher2.doFinal()
        assertArrayEquals(expected[0], hash2)

        // Test each customizer
        customizers.forEachIndexed { index, customizer ->
            val hasher = HasherImpl(HashAlgorithm.KMAC128, key, customizer, outputSize)
            val hash = hasher.hash(input)
            assertArrayEquals(expected[index], hash)
        }

        // Test short key length
        val exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.KMAC128, key.sliceArray(0 until 15), customizers[0], outputSize).hash(input)
        }
        assertEquals("KMAC128 requires a key of at least 16 bytes", exception.message)
    }

    private fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(hexString[i], 16) shl 4)
                + Character.digit(hexString[i + 1], 16)).toByte()
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
        val key = "thisKeyIsAtLeast16Bytes".toByteArray()
        val hasher = HasherImpl(HashAlgorithm.KMAC128, key)
        val signer = SignerImpl(keyPair.private, HashAlgorithm.KMAC128, hasher)
        val signature = signer.sign("test".toByteArray())
        assertNotNull(signature)
    }
}
