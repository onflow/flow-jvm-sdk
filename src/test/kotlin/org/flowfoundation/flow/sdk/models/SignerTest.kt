package org.flowfoundation.flow.sdk.models

import org.flowfoundation.flow.sdk.DomainTag
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.flowfoundation.flow.sdk.Signer
import org.flowfoundation.flow.sdk.Hasher

class SignerTest {
    lateinit var hasher: Hasher

    private lateinit var signer: Signer

    @BeforeEach
    fun setUp() {
        signer = object : Signer {
            override val hasher: Hasher
                get() = this@SignerTest.hasher

            override fun sign(bytes: ByteArray): ByteArray {
                // Mock sign function to return bytes * 2
                return bytes + bytes
            }
        }
    }

    @Test
    fun `Test signWithDomain`() {
        val inputBytes = byteArrayOf(0x01, 0x02, 0x03)
        val domainBytes = byteArrayOf(0x04, 0x05)
        val expectedBytes = signer.sign(domainBytes + inputBytes)

        val actualBytes = signer.signWithDomain(inputBytes, domainBytes)
        assertArrayEquals(expectedBytes, actualBytes)
    }

    @Test
    fun `Test signAsUser`() {
        val inputBytes = byteArrayOf(0x01, 0x02, 0x03)
        val expectedBytes = signer.sign(DomainTag.USER_DOMAIN_TAG + inputBytes)

        val actualBytes = signer.signAsUser(inputBytes)
        assertArrayEquals(expectedBytes, actualBytes)
    }

    @Test
    fun `Test signAsTransaction`() {
        val inputBytes = byteArrayOf(0x01, 0x02, 0x03)
        val expectedBytes = signer.sign(DomainTag.TRANSACTION_DOMAIN_TAG + inputBytes)

        val actualBytes = signer.signAsTransaction(inputBytes)
        assertArrayEquals(expectedBytes, actualBytes)
    }
}
