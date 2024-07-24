package org.onflow.flow.sdk.models

import org.onflow.flow.sdk.HashAlgorithm
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.onflow.flow.sdk.SignatureAlgorithm

class SignatureAndHashingModelTest {
    @Test
    fun `Test hashing algo fromCode`() {
        // based on https://developers.flow.com/build/basics/accounts#signature-and-hash-algorithms
        assertEquals(HashAlgorithm.UNKNOWN, HashAlgorithm.fromCode(-1))
        assertEquals(HashAlgorithm.SHA2_256, HashAlgorithm.fromCode(1))
        assertEquals(HashAlgorithm.SHA3_256, HashAlgorithm.fromCode(3))
    }

    @Test
    fun `Test hashing algo fromCadenceIndex`() {
        // https://cadence-lang.org/docs/language/crypto#hashing
        assertEquals(HashAlgorithm.UNKNOWN, HashAlgorithm.fromCadenceIndex(0))
        assertEquals(HashAlgorithm.SHA2_256, HashAlgorithm.fromCadenceIndex(1))
        assertEquals(HashAlgorithm.SHA2_384, HashAlgorithm.fromCadenceIndex(2))
        assertEquals(HashAlgorithm.SHA3_256, HashAlgorithm.fromCadenceIndex(3))
        assertEquals(HashAlgorithm.SHA3_384, HashAlgorithm.fromCadenceIndex(4))
        assertEquals(HashAlgorithm.KMAC128, HashAlgorithm.fromCadenceIndex(5))
        assertEquals(HashAlgorithm.KECCAK256, HashAlgorithm.fromCadenceIndex(6))
    }

    @Test
    fun `Test signature algo fromCode`() {
        // based on https://developers.flow.com/build/basics/accounts#signature-and-hash-algorithms
        assertEquals(SignatureAlgorithm.ECDSA_P256, SignatureAlgorithm.fromCode(2))
        assertEquals(SignatureAlgorithm.ECDSA_SECP256k1, SignatureAlgorithm.fromCode(3))
        assertEquals(SignatureAlgorithm.UNKNOWN, SignatureAlgorithm.fromCode(-1))
    }

    @Test
    fun `Test signature fromCadenceIndex`() {
        // https://cadence-lang.org/docs/language/crypto#signing-algorithms
        assertEquals(SignatureAlgorithm.ECDSA_P256, SignatureAlgorithm.fromCadenceIndex(1))
        assertEquals(SignatureAlgorithm.ECDSA_SECP256k1, SignatureAlgorithm.fromCadenceIndex(2))
        assertEquals(SignatureAlgorithm.UNKNOWN, SignatureAlgorithm.fromCadenceIndex(-1))
    }

    @Test
    fun `Test signature fromCode with invalid code`() {
        assertEquals(SignatureAlgorithm.UNKNOWN, SignatureAlgorithm.fromCode(0))
    }

    @Test
    fun `Test signature fromCadenceIndex with invalid index`() {
        assertEquals(SignatureAlgorithm.UNKNOWN, SignatureAlgorithm.fromCadenceIndex(4))
    }
}
