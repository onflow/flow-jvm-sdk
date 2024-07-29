package org.onflow.flow.sdk.crypto

import org.onflow.flow.sdk.HashAlgorithm
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
    fun `Get hasher`() {
        val hasher = Crypto.getHasher()
        assertNotNull(hasher)
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

    @Test
    fun `Invalid Hasher input`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.UNKNOWN)
        }
        assertEquals("Unsupported hash algorithm: unknown", exception.message)
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

    @Test
    fun `Sanity check KMAC128`() {
        val input = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        val expected = listOf(
            listOf(
                hexStringToByteArray("E5780B0D3EA6F7D3A429C5706AA43A00FADBD7D49628839E3187243F456EE14E"),
                hexStringToByteArray("3B1FBA963CD8B0B59E8C1A6D71888B7143651AF8BA0A7070C0979E2811324AA5")
            ),
            listOf(
                hexStringToByteArray("4f5967393bd357c13cf1b0aff13c2abe075dd68edee33d6b8cb06f5b2a4d5232c11b439f3c20b20a4f04b0549d9caa10"),
                hexStringToByteArray("99d9364ab5d2b748cb843b27f5a4f4fb06ea87306fa141a676cbb4b39c5c5f12d36b7b76aeea81c4f5876e16e6783e72")
            )
        )
        val key = hexStringToByteArray("404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F")
        val customizers = listOf(
            "".toByteArray(),
            "My Tagged Application".toByteArray()
        )
        val outputSize = listOf(
            // test vector for 32 bytes is taken from https://csrc.nist.gov/CSRC/media/Projects/Cryptographic-Standards-and-Guidelines/documents/examples/Kmac_samples.pdf
            32,
            // test vector for 48 bytes in generated by other trusted libraries
            48
        )

        outputSize.forEachIndexed { indexSize, size ->
            customizers.forEachIndexed { index, customizer ->
                // Test full input processing
                val hasher1 = HasherImpl(HashAlgorithm.KMAC128, key, customizer, size)
                val hash1 = hasher1.hash(input)
                assertArrayEquals(expected[indexSize][index], hash1)

                // Test incremental input processing
                val hasher2 = HasherImpl(HashAlgorithm.KMAC128, key, customizer, size)
                hasher2.update(input, 0, 2)
                hasher2.update(input, 2, input.size - 2)
                val hash2 = hasher2.doFinal(size)
                assertArrayEquals(expected[indexSize][index], hash2)
            }
        }
    }

    @Test
    fun `Test invalid keys`() {
        val exceptionString = "Key must be null"
        val input = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        val key = hexStringToByteArray("404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F")
        // SHA2-256
        var exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.SHA2_256, key, null).hash(input)
        }
        assertEquals(exceptionString, exception.message)
        // SHA3-256
        exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.SHA3_256, key, null).hash(input)
        }
        assertEquals(exceptionString, exception.message)
        // KECCAK-256
        exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.KECCAK256, key, null).hash(input)
        }
        assertEquals(exceptionString, exception.message)

        // KMAC128
        val customizer = "".toByteArray()
        val outputSize = 32

        exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.KMAC128, key.sliceArray(0..<15), customizer, outputSize).hash(input)
        }
        assertEquals("KMAC128 requires a key of at least 16 bytes", exception.message)

        exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.KMAC128, null, customizer, outputSize).hash(input)
        }
        assertEquals("KMAC128 requires a key of at least 16 bytes", exception.message)
    }

    @Test
    fun `Test invalid customizers`() {
        val exceptionString = "Customizer must be null"
        val input = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        val customizer = byteArrayOf(0x00)
        // SHA2-256
        var exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.SHA2_256, null, customizer).hash(input)
        }
        assertEquals(exceptionString, exception.message)
        // SHA3-256
        exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.SHA3_256, null, customizer).hash(input)
        }
        assertEquals(exceptionString, exception.message)
        // KECCAK-256
        exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.KECCAK256, null, customizer).hash(input)
        }
        assertEquals(exceptionString, exception.message)
    }

    @Test
    fun `Test invalid output sizes`() {
        val exceptionString = "Output size must be 32 bytes"
        val input = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        // SHA2-256
        var exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.SHA2_256, null, null, 40).hash(input)
        }
        assertEquals(exceptionString, exception.message)
        // SHA3-256
        exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.SHA3_256, null, null, 40).hash(input)
        }
        assertEquals(exceptionString, exception.message)
        // KECCAK-256
        exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.KECCAK256, null, null, 40).hash(input)
        }
        assertEquals(exceptionString, exception.message)

        // KMAC128
        val key = hexStringToByteArray("404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F")
        val customizer = "".toByteArray()

        exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.KMAC128, key, customizer, 0).hash(input)
        }
        assertEquals("KMAC128 output size must be at least 32 bytes", exception.message)

        exception = assertThrows(IllegalArgumentException::class.java) {
            HasherImpl(HashAlgorithm.KMAC128, key, customizer, 16).hash(input)
        }
        assertEquals("KMAC128 output size must be at least 32 bytes", exception.message)
    }

    private fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        val data = ByteArray(len / 2)
        for (i in 0..<len step 2) {
            data[i / 2] = ((Character.digit(hexString[i], 16) shl 4) + Character.digit(hexString[i + 1], 16)).toByte()
        }
        return data
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