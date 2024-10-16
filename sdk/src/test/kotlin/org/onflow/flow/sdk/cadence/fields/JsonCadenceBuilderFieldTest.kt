package org.onflow.flow.sdk.cadence.fields

import org.onflow.flow.sdk.cadence.*
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JsonCadenceBuilderFieldTest {
    @Test
    fun `Test equality for Field with the same type and value`() {
        val field1 = SampleField(TYPE_STRING, "value")
        val field2 = SampleField(TYPE_STRING, "value")
        assertTrue(field1 == field2)
        assertTrue(field1.hashCode() == field2.hashCode())
    }

    @Test
    fun `Test equality for Field with different values`() {
        val field1 = SampleField(TYPE_STRING, "value1")
        val field2 = SampleField(TYPE_STRING, "value2")
        assertFalse(field1 == field2)
        assertFalse(field1.hashCode() == field2.hashCode())
    }

    @Test
    fun `Test equality for Field with different types`() {
        val field1 = SampleField(TYPE_STRING, "value")
        val field2 = SampleField(TYPE_INT, 42)
        assertFalse(field1 == field2)
        assertFalse(field1.hashCode() == field2.hashCode())
    }

    @Test
    fun `Test equality for Field with null values`() {
        val field1 = SampleField(TYPE_STRING, null)
        val field2 = SampleField(TYPE_STRING, null)
        assertTrue(field1 == field2)
        assertTrue(field1.hashCode() == field2.hashCode())
    }

    @Test
    fun `Test equality for Field with null value and non-null value`() {
        val field1 = SampleField(TYPE_STRING, null)
        val field2 = SampleField(TYPE_STRING, "value")
        assertFalse(field1 == field2)
        assertFalse(field1.hashCode() == field2.hashCode())
    }

    @Test
    fun `Test equality for Field with different types and values`() {
        val field1 = SampleField(TYPE_STRING, "value")
        val field2 = SampleField(TYPE_INT, 42)
        assertFalse(field1 == field2)
        assertFalse(field1.hashCode() == field2.hashCode())
    }

    @Test
    fun `Test decoding unsupported field throws exception`() {
        val unsupportedField = object : Field<Unit>("Unsupported", null) {}
        val expectedMessage = " Can't find right class "

        assertThatThrownBy { unsupportedField.decodeToAny() }
            .isInstanceOf(Exception::class.java)
            .hasMessage(expectedMessage)
    }

    private class SampleField(
        type: String,
        value: Any?
    ) : Field<Any>(type, value)
}
