package org.onflow.flow.sdk.models

import org.onflow.flow.sdk.HashAlgorithm
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HashAlgorithmTest {
    @Test
    fun `Test fromCode`() {
        assertEquals(HashAlgorithm.UNKNOWN, HashAlgorithm.fromCode(-1))
        assertEquals(HashAlgorithm.SHA2_256, HashAlgorithm.fromCode(1))
        assertEquals(HashAlgorithm.SHA3_256, HashAlgorithm.fromCode(3))
    }

    @Test
    fun `Test fromCadenceIndex`() {
        assertEquals(HashAlgorithm.UNKNOWN, HashAlgorithm.fromCadenceIndex(0))
        assertEquals(HashAlgorithm.SHA2_256, HashAlgorithm.fromCadenceIndex(1))
        assertEquals(HashAlgorithm.SHA2_384, HashAlgorithm.fromCadenceIndex(2))
        assertEquals(HashAlgorithm.SHA3_256, HashAlgorithm.fromCadenceIndex(3))
        assertEquals(HashAlgorithm.SHA3_384, HashAlgorithm.fromCadenceIndex(4))
        assertEquals(HashAlgorithm.KECCAK256, HashAlgorithm.fromCadenceIndex(5))
        assertEquals(HashAlgorithm.KMAC128, HashAlgorithm.fromCadenceIndex(6))
    }
}
