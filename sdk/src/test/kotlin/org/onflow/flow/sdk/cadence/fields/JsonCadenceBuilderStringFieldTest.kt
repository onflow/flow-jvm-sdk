package org.onflow.flow.sdk.cadence.fields

import org.onflow.flow.sdk.cadence.StringField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JsonCadenceBuilderStringFieldTest {
    @Test
    fun `Test creating StringField with valid value`() {
        val stringValue = "Hello, World!"
        val stringField = StringField(stringValue)
        assertEquals(stringValue, stringField.value)
    }

    @Test
    fun `Test hashCode`() {
        val stringValue1 = "Hello, World!"
        val stringValue2 = "Hello, World!"
        val stringField1 = StringField(stringValue1)
        val stringField2 = StringField(stringValue2)

        assertEquals(stringField1.hashCode(), stringField2.hashCode())
    }

    @Test
    fun `Test hashCode with different values`() {
        val stringValue1 = "Hello, World!"
        val stringValue2 = "Greetings!"
        val stringField1 = StringField(stringValue1)
        val stringField2 = StringField(stringValue2)

        assertTrue(stringField1.hashCode() != stringField2.hashCode())
    }

    @Test
    fun `Test decoding StringField`() {
        val stringValue = "Hello, World!"
        val stringField = StringField(stringValue)
        val decodedValue = stringField.decodeToAny()

        assertEquals(stringValue, decodedValue)
    }

    @Test
    fun `Test decoding empty StringField`() {
        val stringValue = ""
        val stringField = StringField(stringValue)
        val decodedValue = stringField.decodeToAny()

        assertEquals(stringValue, decodedValue)
    }
}
