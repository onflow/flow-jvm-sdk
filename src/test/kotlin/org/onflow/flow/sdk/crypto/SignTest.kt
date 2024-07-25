package org.onflow.flow.sdk.crypto

import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.onflow.flow.sdk.HashAlgorithm
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.onflow.flow.sdk.SignatureAlgorithm
import java.math.BigInteger
import java.security.Signature
import org. bouncycastle. jce. spec. ECParameterSpec

internal class SignTest {
    @Test
    fun `Can generate KeyPair`() {
        val keyPair = Crypto.generateKeyPair()
        assertNotNull(keyPair.private)
        assertNotNull(keyPair.public)
        assertEquals(keyPair.private.publicKey, keyPair.public)
    }

    @Test
    fun `Test key generation is randomized`() {
        val keyPair1 = Crypto.generateKeyPair()
        val keyPair2 = Crypto.generateKeyPair()
        assertNotEquals(keyPair1.private, keyPair2.private)
        assertNotEquals(keyPair1.public, keyPair2.public)
    }

    @Test
    fun `Can decode keys correctly`() {
        // generate key bytes
        val keyPair = Crypto.generateKeyPair()
        val skBytes = keyPair.private.hex
        val pkBytes = keyPair.public.hex

        // decode private key into the original one
        val decodedPrivateKey = Crypto.decodePrivateKey(skBytes)
        assertEquals(keyPair.private, decodedPrivateKey)
        assertEquals(keyPair.public, decodedPrivateKey.publicKey)

        // decode public key into the original one
        val decodedPublicKey = Crypto.decodePublicKey(pkBytes)
        assertEquals(keyPair.public, decodedPublicKey)
    }

    @Test
    fun `Private key throws exception when invalid`() {
        assertThrows(IllegalArgumentException::class.java) {
            Crypto.decodePrivateKey("invalidKey")
        }
        // TODO: add tests for:
        // - 0 scalar
        // - scalar >= N
    }


    @Test
    fun `Public key throws exception when invalid`() {
        assertThrows(IllegalArgumentException::class.java) {
            Crypto.decodePublicKey("invalidKey")
        }
        // TODO: add tests for:
        // - X or Y not in Z_p
        // - X and Y in Z_p but point not on curve
        // - infinity point
    }

    @Test
    fun `Test derivePublicKey`() { // this is also a sanity test of scalar point multiplication
        val SK = "6e37a39c31a05181bf77919ace790efd0bdbcaf42b5a52871fc112fceb918c95"
        val curves = listOf(
            SignatureAlgorithm.ECDSA_SECP256k1,
            SignatureAlgorithm.ECDSA_P256,
        )
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
    fun `Get signer`() {
        val keyPair = Crypto.generateKeyPair()
        val signer = Crypto.getSigner(keyPair.private)
        assertNotNull(signer)
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
