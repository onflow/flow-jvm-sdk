package com.nftco.flow.sdk.cadence.fields.number

import com.nftco.flow.sdk.cadence.UFix64NumberField
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JsonCadenceBuilderUFix64NumberFieldTest {
    @Test
    fun `Test decoding of UFix64NumberField`() {
        val ufix64Field = UFix64NumberField("123.456")
        assertEquals(123.456, ufix64Field.decodeToAny())
    }

    @Test
    fun `Test decoding of UFix64NumberField with zero value`() {
        val ufix64Field = UFix64NumberField("0.0")
        assertEquals(0.0, ufix64Field.decodeToAny())
    }

    @Test
    fun `Test decoding of UFix64NumberField with large value`() {
        val ufix64Field = UFix64NumberField("9876543210.123456789")
        assertEquals(9876543210.123456789, ufix64Field.decodeToAny())
    }

    @Test
    fun `Test decoding of UFix64NumberField with scientific notation`() {
        val ufix64Field = UFix64NumberField("1.23e3")
        assertEquals(1230.0, ufix64Field.decodeToAny())
    }

    @Test
    fun `Test hashCode on UFix64NumberField`() {
        val ufix64Field1 = UFix64NumberField("123")
        val ufix64Field2 = UFix64NumberField("123")

        assertEquals(ufix64Field1.hashCode(), ufix64Field2.hashCode())
    }

    @Test
    fun `Test decoding of UFix64NumberField with invalid value`() {
        Assertions.assertThatThrownBy { UFix64NumberField("invalidValue").decodeToAny() }
            .isInstanceOf(NumberFormatException::class.java)
    }
}
