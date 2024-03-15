package org.flowfoundation.flow.sdk.cadence.fields.composite

import org.flowfoundation.flow.sdk.cadence.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class JsonCadenceBuilderStructFieldTest {
    @Test
    fun `Test StructField type and value properties`() {
        val fieldValue = CompositeValue("enumId", emptyArray())
        val structField = StructField(fieldValue)

        assertEquals(TYPE_STRUCT, structField.type)
        assertEquals(fieldValue, structField.value)
    }
    @Test
    fun `Test hashCode`() {
        val compositeValue1 = CompositeValue("id1", arrayOf())
        val structField1 = StructField(compositeValue1)

        val compositeValue2 = CompositeValue("id1", arrayOf())
        val structField2 = StructField(compositeValue2)

        assertEquals(structField1.hashCode(), structField2.hashCode())
    }

    @Test
    fun `Test equals`() {
        val compositeValue1 = CompositeValue("id1", arrayOf())
        val structField1 = StructField(compositeValue1)

        val compositeValue2 = CompositeValue("id1", arrayOf())
        val structField2 = StructField(compositeValue2)

        val compositeValue3 = CompositeValue("id2", arrayOf())
        val structField3 = StructField(compositeValue3)

        assertEquals(structField1, structField2)
        assertNotEquals(structField1, structField3)
    }

    @Test
    fun `Test decoding StructField`() {
        val compositeValue = CompositeValue("structId", arrayOf(CompositeAttribute("id", StringField("struct"))))
        val structField = StructField(compositeValue)

        val decodedValue = structField.decodeToAny()
        assertEquals(mapOf("id" to "struct"), decodedValue)
    }
}
