package org.onflow.flow.sdk.cadence.fields.composite

import org.onflow.flow.sdk.cadence.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class JsonCadenceBuilderResourceFieldTest {
    @Test
    fun `Test ResourceField type and value properties`() {
        val fieldValue = CompositeValue("enumId", emptyArray())
        val resourceField = ResourceField(fieldValue)

        assertEquals(TYPE_RESOURCE, resourceField.type)
        assertEquals(fieldValue, resourceField.value)
    }

    @Test
    fun `Test hashCode`() {
        val compositeValue1 = CompositeValue("id1", arrayOf())
        val resourceField1 = ResourceField(compositeValue1)

        val compositeValue2 = CompositeValue("id1", arrayOf())
        val resourceField2 = ResourceField(compositeValue2)

        assertEquals(resourceField1.hashCode(), resourceField2.hashCode())
    }

    @Test
    fun `Test equals`() {
        val compositeValue1 = CompositeValue("id1", arrayOf())
        val resourceField1 = ResourceField(compositeValue1)

        val compositeValue2 = CompositeValue("id1", arrayOf())
        val resourceField2 = ResourceField(compositeValue2)

        val compositeValue3 = CompositeValue("id2", arrayOf())
        val resourceField3 = ResourceField(compositeValue3)

        assertEquals(resourceField1, resourceField2)
        assertNotEquals(resourceField1, resourceField3)
    }

    @Test
    fun `Test decoding ResourceField`() {
        val compositeValue = CompositeValue("resourceId", arrayOf(CompositeAttribute("id", StringField("resource"))))
        val resourceField = ResourceField(compositeValue)

        val decodedValue = resourceField.decodeToAny()
        assertEquals(mapOf("id" to "resource"), decodedValue)
    }
}
