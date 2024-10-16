package org.onflow.flow.sdk.cadence.fields

import org.onflow.flow.sdk.cadence.*
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JsonCadenceBuilderOptionalFieldTest {
    @Test
    fun `Test creating OptionalField with null value`() {
        val optionalField = OptionalField(null)
        assertNull(optionalField.value)
        assertEquals(TYPE_OPTIONAL, optionalField.type)
    }

    @Test
    fun `Test creating OptionalField with non-null value`() {
        val innerField = StringField("Hello, World!")
        val optionalField = OptionalField(innerField)

        assertEquals(innerField, optionalField.value)
        assertEquals(TYPE_OPTIONAL, optionalField.type)
    }

    @Test
    fun `Test equality of OptionalField`() {
        val field1 = StringField("Value")
        val field2 = StringField("Value")
        val optionalField1 = OptionalField(field1)
        val optionalField2 = OptionalField(field2)
        val optionalField3 = OptionalField(null)

        assertTrue(optionalField1 == optionalField2)
        assertTrue(optionalField1.hashCode() == optionalField2.hashCode())

        assertTrue(optionalField1 != optionalField3)
        assertTrue(optionalField1.hashCode() != optionalField3.hashCode())
    }

    @Test
    fun `Test decoding OptionalField`() {
        val innerField = StringField("Hello, World!")
        val optionalField = OptionalField(innerField)

        val decodedValue = optionalField.decodeToAny()
        assertEquals(innerField.value, decodedValue)
    }

    @Test
    fun `Test decoding null OptionalField`() {
        val optionalField = OptionalField(null)

        val decodedValue = optionalField.decodeToAny()
        assertNull(decodedValue)
    }

    @Test
    fun `Test decoding OptionalField with invalid inner field`() {
        val invalidInnerField = NumberField(TYPE_INT, "abc")
        Assertions
            .assertThatThrownBy { OptionalField(invalidInnerField).decodeToAny() }
            .isInstanceOf(Exception::class.java)
    }

    @Test
    fun `Test decoding OptionalField with invalid null inner field`() {
        Assertions
            .assertThatThrownBy { OptionalField(null as Field<*>).decodeToAny() }
            .isInstanceOf(Exception::class.java)
    }
}
