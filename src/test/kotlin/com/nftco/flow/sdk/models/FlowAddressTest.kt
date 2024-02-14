package com.nftco.flow.sdk.models

import com.nftco.flow.sdk.FlowAddress
import com.nftco.flow.sdk.hexToBytes
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class FlowAddressTest {
    @Test
    fun `Can create FlowAddress from a hex string`() {
        Assertions.assertThat(FlowAddress("0x01").base16Value).isEqualTo("0000000000000001")
        Assertions.assertThat(FlowAddress("01").bytes).isEqualTo("0000000000000001".hexToBytes())
        Assertions.assertThat(FlowAddress("00").base16Value).isEqualTo("0000000000000000")
        Assertions.assertThat(FlowAddress("01").base16Value).isEqualTo("0000000000000001")
        Assertions.assertThat(FlowAddress("10").base16Value).isEqualTo("0000000000000010")
        Assertions.assertThat(FlowAddress("0x18eb4ee6b3c026d3").base16Value).isEqualTo("18eb4ee6b3c026d3")
        Assertions.assertThat(FlowAddress("0x18eb4ee6b3c026d3").formatted).isEqualTo("0x18eb4ee6b3c026d3")
        Assertions.assertThat(FlowAddress("18eb4ee6b3c026d3").formatted).isEqualTo("0x18eb4ee6b3c026d3")
    }

    @Test
    fun `Can create FlowAddress from a byte array`() {
        Assertions.assertThat(FlowAddress.of("0x01".hexToBytes()).base16Value).isEqualTo("0000000000000001")
        Assertions.assertThat(FlowAddress.of("01".hexToBytes()).bytes).isEqualTo("0000000000000001".hexToBytes())
        Assertions.assertThat(FlowAddress.of("00".hexToBytes()).base16Value).isEqualTo("0000000000000000")
        Assertions.assertThat(FlowAddress.of("01".hexToBytes()).base16Value).isEqualTo("0000000000000001")
        Assertions.assertThat(FlowAddress.of("10".hexToBytes()).base16Value).isEqualTo("0000000000000010")
        Assertions.assertThat(FlowAddress.of("0x18eb4ee6b3c026d3".hexToBytes()).base16Value).isEqualTo("18eb4ee6b3c026d3")
        Assertions.assertThat(FlowAddress.of("0x18eb4ee6b3c026d3".hexToBytes()).formatted).isEqualTo("0x18eb4ee6b3c026d3")
        Assertions.assertThat(FlowAddress.of("18eb4ee6b3c026d3".hexToBytes()).formatted).isEqualTo("0x18eb4ee6b3c026d3")
    }

    @Test
    fun `Throws error creating FlowAddress from invalid input`() {
        Assertions.assertThatThrownBy { FlowAddress.of("0x1".hexToBytes()).base16Value }
        Assertions.assertThatThrownBy { FlowAddress.of("x".hexToBytes()).base16Value }
        Assertions.assertThatThrownBy { FlowAddress.of("0k".hexToBytes()).base16Value }
        Assertions.assertThatThrownBy { FlowAddress.of("1".hexToBytes()).base16Value }
        Assertions.assertThatThrownBy { FlowAddress.of("0".hexToBytes()).base16Value }
        Assertions.assertThatThrownBy { FlowAddress.of("18eb4ee6b3c026d31".hexToBytes()).bytes }
        Assertions.assertThatThrownBy { FlowAddress("0x1").base16Value }
        Assertions.assertThatThrownBy { FlowAddress("x").base16Value }
        Assertions.assertThatThrownBy { FlowAddress("1").base16Value }
        Assertions.assertThatThrownBy { FlowAddress("0").base16Value }
        Assertions.assertThatThrownBy { FlowAddress("18eb4ee6b3c026d31").bytes }
    }

    @Test
    fun `Test equality of FlowAddress instances`() {
        val address1 = FlowAddress.of(byteArrayOf(0x12, 0x34, 0x56, 0x78, 0x9A.toByte(), 0xBC.toByte(), 0xDE.toByte(), 0xF0.toByte()))
        val address2 = FlowAddress.of(byteArrayOf(0x12, 0x34, 0x56, 0x78, 0x9A.toByte(), 0xBC.toByte(), 0xDE.toByte(), 0xF0.toByte()))

        val address3 = FlowAddress.of(byteArrayOf(0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(), 0x01, 0x23, 0x45, 0x67, 0x89.toByte()))

        assertEquals(address1, address2)
        assertNotEquals(address1, address3)
    }

    @Test
    fun `Test hash code of FlowAddress`() {
        val address = FlowAddress.of(byteArrayOf(0x12, 0x34, 0x56, 0x78, 0x9A.toByte(), 0xBC.toByte(), 0xDE.toByte(), 0xF0.toByte()))

        assertEquals(address.bytes.contentHashCode(), address.hashCode())
    }
}
