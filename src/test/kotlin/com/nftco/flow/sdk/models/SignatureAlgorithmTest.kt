package com.nftco.flow.sdk.models

import com.nftco.flow.sdk.SignatureAlgorithm
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SignatureAlgorithmTest {
    @Test
    fun `Test fromCode`() {
        assertEquals(SignatureAlgorithm.ECDSA_P256, SignatureAlgorithm.fromCode(2))
        assertEquals(SignatureAlgorithm.ECDSA_SECP256k1, SignatureAlgorithm.fromCode(3))
        assertEquals(SignatureAlgorithm.UNKNOWN, SignatureAlgorithm.fromCode(-1))
    }

    @Test
    fun `Test fromCadenceIndex`() {
        assertEquals(SignatureAlgorithm.ECDSA_P256, SignatureAlgorithm.fromCadenceIndex(1))
        assertEquals(SignatureAlgorithm.ECDSA_SECP256k1, SignatureAlgorithm.fromCadenceIndex(2))
        assertEquals(SignatureAlgorithm.UNKNOWN, SignatureAlgorithm.fromCadenceIndex(-1))
    }

    @Test
    fun `Test fromCode with invalid code`() {
        assertEquals(SignatureAlgorithm.UNKNOWN, SignatureAlgorithm.fromCode(0))
    }

    @Test
    fun `Test fromCadenceIndex with invalid index`() {
        assertEquals(SignatureAlgorithm.UNKNOWN, SignatureAlgorithm.fromCadenceIndex(4))
    }
}
