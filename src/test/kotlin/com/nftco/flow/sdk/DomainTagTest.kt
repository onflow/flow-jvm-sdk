package com.nftco.flow.sdk

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

class DomainTagTest {
    @Test
    fun `Test TRANSACTION_DOMAIN_TAG`() {
        val expected = "FLOW-V0.0-transaction".toByteArray(Charsets.UTF_8)
        val paddedExpected = expected + ByteArray(32 - expected.size)
        assertArrayEquals(paddedExpected, DomainTag.TRANSACTION_DOMAIN_TAG)
    }

    @Test
    fun `Test USER_DOMAIN_TAG`() {
        val expected = "FLOW-V0.0-user".toByteArray(Charsets.UTF_8)
        val paddedExpected = expected + ByteArray(32 - expected.size)
        assertArrayEquals(paddedExpected, DomainTag.USER_DOMAIN_TAG)
    }

    @Test
    fun `Test normalize`() {
        val input = "test"
        val expected = "test".toByteArray(Charsets.UTF_8) + ByteArray(28) // Padded to 32 bytes
        assertArrayEquals(expected, DomainTag.normalize(input))
    }

    @Test
    fun `Test normalize with long tag`() {
        val longTag = "This is a long tag that exceeds 32 characters"
        assertThatThrownBy { DomainTag.normalize(longTag) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Domain tags cannot be longer than 32 characters")
    }

    @Test
    fun `Test normalize with exactly 32 characters`() {
        val tag = "'Exactly 32 characters long tag'"
        val expected = tag.toByteArray(Charsets.UTF_8)
        assertArrayEquals(expected, DomainTag.normalize(tag))
    }
}
