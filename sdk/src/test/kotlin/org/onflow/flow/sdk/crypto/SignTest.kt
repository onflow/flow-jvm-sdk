package org.onflow.flow.sdk.crypto

import org.onflow.flow.sdk.HashAlgorithm
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.onflow.flow.sdk.SignatureAlgorithm
import org.onflow.flow.sdk.bytesToHex
import kotlin.random.Random

internal data class SupportedCurve(
    val curve: SignatureAlgorithm,
    val privateKeySize: Int,
    val publicKeySize: Int,
    val curveOrder: String
)

internal class SignTest {
    // all supported curves of the lib
    val supportedAlgos = listOf(
        SupportedCurve(
            SignatureAlgorithm.ECDSA_SECP256k1, 32, 64,
            "fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141"
        ),
        SupportedCurve(
            SignatureAlgorithm.ECDSA_P256, 32, 64,
            "ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551"
        )
    )
    val loopCount = 100

    @Test
    fun `Test KeyPair generation`() {
        supportedAlgos.forEachIndexed { _, algo ->
            for (i in 0..loopCount) {
                // sanity check of matching public keys
                val keyPair1 = Crypto.generateKeyPair(algo.curve)
                assertNotNull(keyPair1.private)
                assertNotNull(keyPair1.public)
                assertEquals(keyPair1.private.publicKey, keyPair1.public)
                assertEquals(keyPair1.private.hex.length, algo.privateKeySize * 2)
                assertEquals(keyPair1.public.hex.length, algo.publicKeySize * 2)

                // sanity check of internal randomization
                val keyPair2 = Crypto.generateKeyPair(algo.curve)
                assertNotEquals(keyPair1.private, keyPair2.private)
                assertNotEquals(keyPair1.public, keyPair2.public)
            }
        }
    }

    @Test
    fun `Can decode keys correctly`() {
        supportedAlgos.forEachIndexed { _, algo ->
            for (i in 0..loopCount) {
                // generate key bytes
                val keyPair = Crypto.generateKeyPair(algo.curve)
                val skHex = keyPair.private.hex
                val pkHex = keyPair.public.hex
                assertEquals(skHex.length, algo.privateKeySize * 2)
                assertEquals(pkHex.length, algo.publicKeySize * 2)

                // decode private key into the original one
                val decodedPrivateKey = Crypto.decodePrivateKey(skHex, algo.curve)
                assertEquals(keyPair.private, decodedPrivateKey)
                assertEquals(keyPair.public, decodedPrivateKey.publicKey)
                assertEquals(decodedPrivateKey.hex.length, algo.privateKeySize * 2)

                // decode public key into the original one
                val decodedPublicKey = Crypto.decodePublicKey(pkHex, algo.curve)
                assertEquals(keyPair.public, decodedPublicKey)
                assertEquals(decodedPublicKey.hex.length, algo.publicKeySize * 2)
            }
        }
    }

    @Test
    fun `Private key decoding throws exception when invalid`() {
        supportedAlgos.forEachIndexed { _, algo ->
            // non-hex key string of correct length
            val nonHexKey = "GG" + ByteArray(algo.privateKeySize - 1).bytesToHex()
            assertThrows(IllegalArgumentException::class.java) {
                Crypto.decodePrivateKey(nonHexKey, algo.curve)
            }
            // zero scalar
            val zeroKey = ByteArray(algo.privateKeySize).bytesToHex()
            assertThrows(IllegalArgumentException::class.java) {
                Crypto.decodePrivateKey(zeroKey, algo.curve)
            }
            // scalar equal to N
            assertThrows(IllegalArgumentException::class.java) {
                Crypto.decodePrivateKey(algo.curveOrder, algo.curve)
            }
            // incorrect length
            val longKey = Random.nextBytes(algo.privateKeySize + 1).bytesToHex()
            assertThrows(IllegalArgumentException::class.java) {
                Crypto.decodePrivateKey(longKey, algo.curve)
            }
            val shortKey = Random.nextBytes(algo.privateKeySize - 1).bytesToHex()
            assertThrows(IllegalArgumentException::class.java) {
                Crypto.decodePrivateKey(shortKey, algo.curve)
            }
        }
    }

    @Test
    fun `Public key decoding throws exception when invalid`() {
        supportedAlgos.forEachIndexed { _, algo ->
            val nonHexKey = "GG" + ByteArray(algo.publicKeySize - 1).bytesToHex()
            // non-hex key string of correct length
            assertThrows(IllegalArgumentException::class.java) {
                Crypto.decodePublicKey(nonHexKey, algo.curve)
            }
            // zero key (this isn't necessary the infinity point)
            val zeroKey = ByteArray(algo.publicKeySize).bytesToHex()
            assertThrows(IllegalArgumentException::class.java) {
                Crypto.decodePublicKey(zeroKey, algo.curve)
            }
            // incorrect length
            val longKey = Random.nextBytes(algo.publicKeySize + 1).bytesToHex()
            assertThrows(IllegalArgumentException::class.java) {
                Crypto.decodePublicKey(longKey, algo.curve)
            }
            val shortKey = Random.nextBytes(algo.publicKeySize - 1).bytesToHex()
            assertThrows(IllegalArgumentException::class.java) {
                Crypto.decodePublicKey(shortKey, algo.curve)
            }
            // point is not on curve (x and y are in Z_p_
            var invalidPoint = ByteArray(algo.publicKeySize)
            invalidPoint[0] = 6 // x = 6
            invalidPoint[algo.publicKeySize / 2] = 4 // y = 4
            assertThrows(IllegalArgumentException::class.java) {
                Crypto.decodePublicKey(invalidPoint.bytesToHex(), algo.curve)
            }
        }
    }

    @Test
    fun `Test derivePublicKey`() { // this is also a sanity test of scalar point multiplication
        val SK = "6e37a39c31a05181bf77919ace790efd0bdbcaf42b5a52871fc112fceb918c95"

        val expectedPKs = listOf(
            "36f292f6c287b6e72ca8128465647c7f88730f84ab27a1e934dbd2da753930fa39a09ddcf3d28fb30cc683de3fc725e095ec865c3d41aef6065044cb12b1ff61",
            "78a80dfe190a6068be8ddf05644c32d2540402ffc682442f6a9eeb96125d86813789f92cf4afabf719aaba79ecec54b27e33a188f83158f6dd15ecb231b49808"
        )

        supportedAlgos.forEachIndexed { index, algo ->
            // decode the private key
            val sk = Crypto.decodePrivateKey(SK, algo.curve)
            // get the public key using the internal scalar point multiplication
            val pkHex = sk.publicKey.hex
            assertEquals(expectedPKs[index], pkHex)
        }
    }

    @Test
    fun `Test signer compatibility with hash algorithms`() {
        val supportedHashes = listOf(
            HashAlgorithm.SHA2_256,
            HashAlgorithm.SHA3_256,
            HashAlgorithm.KECCAK256
        )
        val nonSupportedHashes = listOf(
            HashAlgorithm.KMAC128,
            HashAlgorithm.SHA2_384,
            HashAlgorithm.SHA3_384,
        )
        supportedAlgos.forEachIndexed { _, algo ->
            supportedHashes.forEachIndexed { _, hashAlgo ->
                val keyPair = Crypto.generateKeyPair(algo.curve)
                val signer = Crypto.getSigner(keyPair.private, hashAlgo)
                val signature = signer.sign("test".toByteArray())
                assertNotNull(signature)
            }
            nonSupportedHashes.forEachIndexed { _, hashAlgo ->
                val keyPair = Crypto.generateKeyPair(algo.curve)
                val exception = assertThrows(IllegalArgumentException::class.java) {
                    Crypto.getSigner(keyPair.private, hashAlgo)
                }
                assertEquals("Unsupported hash algorithm: ${hashAlgo.algorithm}", exception.message)
            }
        }
    }

    @Test
    fun `Test signer correctness`() {
        val supportedHashes = listOf(
            HashAlgorithm.SHA2_256,
            HashAlgorithm.SHA3_256,
            HashAlgorithm.KECCAK256
        )

        supportedAlgos.forEachIndexed { _, algo ->
            supportedHashes.forEachIndexed { _, hashAlgo ->
                val keyPair = Crypto.generateKeyPair(algo.curve)
                val otherKeyPair = Crypto.generateKeyPair(algo.curve)
                for (i in 0..loopCount) {
                    val message = Random.nextBytes(20)
                    // signatures must be valid
                    val signer = Crypto.getSigner(keyPair.private, hashAlgo)
                    val signature = signer.sign(message)
                    assertTrue(keyPair.public.verify(signature, message, hashAlgo))
                    // signatures against a different message must fail
                    val otherMessage = Random.nextBytes(16)
                    assertFalse(keyPair.public.verify(signature, otherMessage, hashAlgo))
                    // signatures against a different key must fail
                    assertFalse(otherKeyPair.public.verify(signature, message, hashAlgo))
                }
            }
        }
    }

    @Test
    fun `Test signer with invalid algo keys`() {
        val keyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256)

        // get signer with invalid-algo key
        val invalidSK = PrivateKey(
            key = keyPair.private.key,
            algo = SignatureAlgorithm.UNKNOWN,
            hex = "",
            publicKey = keyPair.public
        )
        var exception = assertThrows(IllegalArgumentException::class.java) {
            Crypto.getSigner(invalidSK, HashAlgorithm.SHA2_256)
        }
        assertEquals(exception.message, "algorithm ${SignatureAlgorithm.UNKNOWN} is not supported")

        // verify with invalid-algo key
        val invalidPK = PublicKey(
            key = keyPair.public.key,
            algo = SignatureAlgorithm.UNKNOWN,
            hex = ""
        )
        exception = assertThrows(IllegalArgumentException::class.java) {
            invalidPK.verify("".toByteArray(), "".toByteArray(), HashAlgorithm.SHA2_256)
        }
        assertEquals(exception.message, "algorithm ${SignatureAlgorithm.UNKNOWN} is not supported")
    }
}
