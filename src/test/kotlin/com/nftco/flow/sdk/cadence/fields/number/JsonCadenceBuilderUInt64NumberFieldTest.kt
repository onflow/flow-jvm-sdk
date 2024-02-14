package com.nftco.flow.sdk.cadence.fields.number

import com.nftco.flow.sdk.cadence.UInt64NumberField
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JsonCadenceBuilderUInt64NumberFieldTest {
    @Test
    fun `Test decoding of UInt64NumberField with valid value`() {
        val uint64Field = UInt64NumberField("12345678901234567890")
        assertEquals(12345678901234567890UL, uint64Field.decodeToAny())
    }

    @Test
    fun `Test decoding of UInt64NumberField with zero value`() {
        val uint64Field = UInt64NumberField("0")
        assertEquals(0UL, uint64Field.decodeToAny())
    }

    @Test
    fun `Test decoding of UInt64NumberField with maximum value`() {
        val uint64Field = UInt64NumberField("18446744073709551615")
        assertEquals(18446744073709551615UL, uint64Field.decodeToAny())
    }

    @Test
    fun `Test hashCode on UInt64NumberField`() {
        val uint64Field1 = UInt64NumberField("123")
        val uint64Field2 = UInt64NumberField("123")

        assertEquals(uint64Field1.hashCode(), uint64Field2.hashCode())
    }

    @Test
    fun `Test decoding of UInt64NumberField with invalid value`() {
        Assertions.assertThatThrownBy { UInt64NumberField("invalidValue").decodeToAny() }
            .isInstanceOf(NumberFormatException::class.java)
    }
}
