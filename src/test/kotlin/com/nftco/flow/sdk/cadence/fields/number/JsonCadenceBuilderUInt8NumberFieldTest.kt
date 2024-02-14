package com.nftco.flow.sdk.cadence.fields.number

import com.nftco.flow.sdk.cadence.UInt8NumberField
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JsonCadenceBuilderUInt8NumberFieldTest {
    @Test
    fun `Test creating UInt8NumberField with valid value`() {
        val uint8Field = UInt8NumberField("42")
        assertEquals("42", uint8Field.value)
    }

    @Test
    fun `Test decoding of UInt8NumberField with maximum value`() {
        val uint8Field = UInt8NumberField("255")
        assertEquals(255U, uint8Field.decodeToAny())
    }

    @Test
    fun `Test hashCode on UInt8NumberField`() {
        val uint8Field1 = UInt8NumberField("123")
        val uint8Field2 = UInt8NumberField("123")
        assertEquals(uint8Field1.hashCode(), uint8Field2.hashCode())
    }

    @Test
    fun `Test creating UInt8NumberField with invalid value`() {
        Assertions.assertThatThrownBy { UInt8NumberField("invalidValue").decodeToAny() }
            .isInstanceOf(NumberFormatException::class.java)
    }
}
