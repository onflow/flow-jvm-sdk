package org.onflow.flow.sdk.crypto

import org.onflow.flow.sdk.HashAlgorithm
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.onflow.flow.sdk.SignatureAlgorithm
import org.onflow.flow.sdk.bytesToHex
import kotlin.random.Random


internal class SignTest {
    // all supported curves of the lib
    val curves = listOf(
        SignatureAlgorithm.ECDSA_SECP256k1,
        SignatureAlgorithm.ECDSA_P256,
    )

    @Test
    fun `Test KeyPair generation`() {
        curves.forEachIndexed { _, curve ->
            // sanity check of matching public keys
            val keyPair1 = Crypto.generateKeyPair(curve)
            assertNotNull(keyPair1.private)
            assertNotNull(keyPair1.public)
            assertEquals(keyPair1.private.publicKey, keyPair1.public)

            // sanity check of internal randomization
            val keyPair2 = Crypto.generateKeyPair(curve)
            assertNotEquals(keyPair1.private, keyPair2.private)
            assertNotEquals(keyPair1.public, keyPair2.public)
        }
    }


    @Test
    fun `Can decode keys correctly`() {
        curves.forEachIndexed { _, curve ->
            // generate key bytes
            val keyPair = Crypto.generateKeyPair(curve)
            val skBytes = keyPair.private.hex
            val pkBytes = keyPair.public.hex

            // decode private key into the original one
            val decodedPrivateKey = Crypto.decodePrivateKey(skBytes, curve)
            assertEquals(keyPair.private, decodedPrivateKey)
            assertEquals(keyPair.public, decodedPrivateKey.publicKey)

            // decode public key into the original one
            val decodedPublicKey = Crypto.decodePublicKey(pkBytes, curve)
            assertEquals(keyPair.public, decodedPublicKey)
        }
    }

    @Test
    fun `Private key decoding throws exception when invalid`() {
        curves.forEachIndexed { index, curve ->
            assertThrows(IllegalArgumentException::class.java) {
                Crypto.decodePrivateKey("invalidKey", curve)
            }
            // TODO: add tests for:
            // - 0 scalar
            // - scalar >= N
        }
    }


    @Test
    fun `Public key decoding throws exception when invalid`() {
        curves.forEachIndexed { index, curve ->
            assertThrows(IllegalArgumentException::class.java) {
                Crypto.decodePublicKey("invalidKey", curve)
            }
            // TODO: add tests for:
            // - X or Y not in Z_p
            // - X and Y in Z_p but point not on curve
            // - infinity point
        }
    }

    @Test
    fun `Test derivePublicKey`() { // this is also a sanity test of scalar point multiplication
        val SK = "6e37a39c31a05181bf77919ace790efd0bdbcaf42b5a52871fc112fceb918c95"

        val expectedPKs = listOf(
            "36f292f6c287b6e72ca8128465647c7f88730f84ab27a1e934dbd2da753930fa39a09ddcf3d28fb30cc683de3fc725e095ec865c3d41aef6065044cb12b1ff61",
            "78a80dfe190a6068be8ddf05644c32d2540402ffc682442f6a9eeb96125d86813789f92cf4afabf719aaba79ecec54b27e33a188f83158f6dd15ecb231b49808"
        )

        curves.forEachIndexed { index, curve ->
            // decode the private key
            val sk = Crypto.decodePrivateKey(SK, curve)
            val k = sk.key
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
            // TODO: uncomment after merging master
            //HashAlgorithm.SHA2_384,
            //HashAlgorithm.SHA3_384,
        )
        curves.forEachIndexed { _, curve ->
            supportedHashes.forEachIndexed { _, hashAlgo ->
                val keyPair = Crypto.generateKeyPair(curve)
                val signer = Crypto.getSigner(keyPair.private, hashAlgo)
                val signature = signer.sign("test".toByteArray())
                assertNotNull(signature)
            }
            nonSupportedHashes.forEachIndexed { _, hashAlgo ->
                val keyPair = Crypto.generateKeyPair(curve)
                val signer = Crypto.getSigner(keyPair.private, hashAlgo)
                val exception = assertThrows(IllegalArgumentException::class.java) {
                    signer.sign("test".toByteArray())
                }
                assertEquals(exception.message, "Unsupported hash algorithm: ${hashAlgo.algorithm}")
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

        val loopCount = 100
        curves.forEachIndexed { _, curve ->
            supportedHashes.forEachIndexed { _, hashAlgo ->
                val keyPair = Crypto.generateKeyPair(curve)
                for (i in 0.. loopCount) {
                    val message =  Random.nextBytes(10)
                    // signatures must be valid
                    val signer = Crypto.getSigner(keyPair.private, hashAlgo)
                    val signature = signer.sign(message)
                    val valid = keyPair.public.verify(signature, message, hashAlgo)
                    assertTrue(valid)
                    // TODO: signatures against a different message must fail
                    // TODO: signatures against a different key must fail
                    // TODO: getSigner call with invalid algo
                }
            }
        }
    }
}
