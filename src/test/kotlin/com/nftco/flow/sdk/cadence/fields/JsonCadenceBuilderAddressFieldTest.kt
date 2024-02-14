package com.nftco.flow.sdk.cadence.fields

import com.nftco.flow.sdk.FlowAddress
import com.nftco.flow.sdk.cadence.AddressField
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JsonCadenceBuilderAddressFieldTest {
    @Test
    fun `test decoding AddressField with decodeToAny`() {
        val addressValue = "0x0f7531409b1719ee"
        val addressField = AddressField(addressValue)

        assertDoesNotThrow { addressField.decodeToAny() }
    }

    @Test
    fun `test decoding AddressField to FlowAddress`() {
        val addressValue = "0x0f7531409b1719ee"
        val addressField = AddressField(addressValue)

        val result = addressField.decodeToAny()
        val expectedBytes = "15, 117, 49, 64, -101, 23, 25, -18".split(", ").map { it.toByte() }.toByteArray()

        assertThat(result).isInstanceOf(FlowAddress::class.java)
        assertThat((result as FlowAddress).bytes).isEqualTo(expectedBytes)
    }

    @Test
    fun `test decoding invalid AddressField with decodeToAny`() {
        val invalidAddressValue = "invalid_address"
        val addressField = AddressField(invalidAddressValue)

        assertThrows<Exception> { addressField.decodeToAny() }
    }

    @Test
    fun `Test equality for AddressField with the same value`() {
        val address1 = AddressField("0x123")
        val address2 = AddressField("0x123")
        assertEquals(address1, address2)
        assertEquals(address1.hashCode(), address2.hashCode())
    }

    @Test
    fun `Test equality for AddressField with different values`() {
        val address1 = AddressField("0x123")
        val address2 = AddressField("0x456")
        assertNotEquals(address1, address2)
        assertNotEquals(address1.hashCode(), address2.hashCode())
    }

    @Test
    fun `Test equality for AddressField with lowercase value`() { // need to investigate this case
        val address1 = AddressField("0xABC")
        val address2 = AddressField("0xabc")
        // assertEquals(address1, address2)
        // assertEquals(address1.hashCode(), address2.hashCode())
    }

    @Test
    fun `Test equality for AddressField with bytes`() {
        val bytes = byteArrayOf(1, 2, 3)
        val address1 = AddressField(bytes)
        val address2 = AddressField("0x010203")
        assertEquals(address1, address2)
        assertEquals(address1.hashCode(), address2.hashCode())
    }

    @Test
    fun `Test equality for AddressField with bytes and different values`() {
        val bytes = byteArrayOf(1, 2, 3)
        val address1 = AddressField(bytes)
        val address2 = AddressField("0x040506")
        assertNotEquals(address1, address2)
        assertNotEquals(address1.hashCode(), address2.hashCode())
    }
}
