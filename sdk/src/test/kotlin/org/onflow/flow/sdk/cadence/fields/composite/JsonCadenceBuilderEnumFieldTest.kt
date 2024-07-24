package org.onflow.flow.sdk.cadence.fields.composite

import org.onflow.flow.sdk.cadence.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class JsonCadenceBuilderEnumFieldTest {
    @Test
    fun `Test EnumField type and value properties`() {
        val fieldValue = CompositeValue("enumId", emptyArray())
        val enumField = EnumField(fieldValue)

        assertEquals(TYPE_ENUM, enumField.type)
        assertEquals(fieldValue, enumField.value)
    }
    @Test
    fun `Test hashCode`() {
        val compositeValue1 = CompositeValue("id1", arrayOf())
        val enumField1 = EnumField(compositeValue1)

        val compositeValue2 = CompositeValue("id1", arrayOf())
        val enumField2 = EnumField(compositeValue2)

        assertEquals(enumField1.hashCode(), enumField2.hashCode())
    }

    @Test
    fun `Test equals`() {
        val compositeValue1 = CompositeValue("id1", arrayOf())
        val enumField1 = EnumField(compositeValue1)

        val compositeValue2 = CompositeValue("id1", arrayOf())
        val enumField2 = EnumField(compositeValue2)

        val compositeValue3 = CompositeValue("id2", arrayOf())
        val enumField3 = EnumField(compositeValue3)

        assertEquals(enumField1, enumField2)
        assertNotEquals(enumField1, enumField3)
    }

    @Test
    fun `Test decoding EnumField`() {
        val compositeValue = CompositeValue("enumId", arrayOf(CompositeAttribute("id", StringField("enum"))))
        val enumField = EnumField(compositeValue)

        val decodedValue = enumField.decodeToAny()
        assertEquals(mapOf("id" to "enum"), decodedValue)
    }
}
