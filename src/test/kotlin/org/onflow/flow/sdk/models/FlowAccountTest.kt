package org.onflow.flow.sdk.models

import com.google.protobuf.ByteString
import org.onflow.flow.sdk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.onflow.protobuf.entities.AccountOuterClass
import java.math.BigDecimal

class FlowAccountTest {
    @Test
    fun `Test building FlowAccount from AccountOuterClass`() {
        val addressString = "12345678"

        val accountBuilder = AccountOuterClass.Account.newBuilder()
            .setAddress(ByteString.copyFromUtf8(addressString))
            .setBalance(1000000000)
            .addKeys(AccountOuterClass.AccountKey.newBuilder().setIndex(0).setPublicKey(ByteString.copyFromUtf8("0x1234")).build())
            .putContracts("contract1", ByteString.copyFromUtf8("0x123456"))
            .putContracts("contract2", ByteString.copyFromUtf8("0xabcdef"))

        val flowAccount = FlowAccount.of(accountBuilder.build())

        assertEquals("12345678", flowAccount.address.stringValue)
        assertEquals(BigDecimal("10.00000000"), flowAccount.balance)
        assertEquals(1, flowAccount.keys.size)
        assertEquals("0x1234", flowAccount.keys[0].publicKey.stringValue)
        assertEquals(2, flowAccount.contracts.size)
        assertEquals("0x123456", flowAccount.contracts["contract1"]?.stringValue)
        assertEquals("0xabcdef", flowAccount.contracts["contract2"]?.stringValue)
    }

    @Test
    fun `Test getKeyIndex`() {
        val keys = listOf(
            FlowAccountKey(1, FlowPublicKey("0x1234".toByteArray()), SignatureAlgorithm.ECDSA_P256, HashAlgorithm.SHA3_256, 1),
            FlowAccountKey(2, FlowPublicKey("0x5678".toByteArray()), SignatureAlgorithm.ECDSA_P256, HashAlgorithm.SHA3_256, 1),
            FlowAccountKey(3, FlowPublicKey("0x9abc".toByteArray()), SignatureAlgorithm.ECDSA_P256, HashAlgorithm.SHA3_256, 1)
        )

        val flowAccount = FlowAccount(FlowAddress("0x123456789abcdef0"), BigDecimal.ZERO, FlowCode("0xabcdef1234567890".toByteArray()), keys, emptyMap())

        assertEquals(1, flowAccount.getKeyIndex("0x1234".toByteArray().bytesToHex()))
        assertEquals(2, flowAccount.getKeyIndex("0x5678".toByteArray().bytesToHex()))
        assertEquals(3, flowAccount.getKeyIndex("0x9abc".toByteArray().bytesToHex()))

        assertEquals(-1, flowAccount.getKeyIndex("abcd"))
    }
}
