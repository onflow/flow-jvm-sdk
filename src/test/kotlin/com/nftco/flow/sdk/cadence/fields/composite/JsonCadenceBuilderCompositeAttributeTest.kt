package com.nftco.flow.sdk.cadence.fields.composite

import com.nftco.flow.sdk.cadence.CompositeAttribute
import com.nftco.flow.sdk.cadence.StringField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class JsonCadenceBuilderCompositeAttributeTest {
    @Test
    fun `Test creating CompositeAttribute with valid values`() {
        val stringValue = StringField("test")
        val compositeAttribute = CompositeAttribute("name", stringValue)

        assertEquals("name", compositeAttribute.name)
        assertEquals(stringValue, compositeAttribute.value)
    }

    @Test
    fun `Test hashCode`() {
        val stringValue1 = StringField("test")
        val stringValue2 = StringField("test")
        val compositeAttribute1 = CompositeAttribute("name", stringValue1)
        val compositeAttribute2 = CompositeAttribute("name", stringValue2)

        assertEquals(compositeAttribute1.hashCode(), compositeAttribute2.hashCode())
    }

    @Test
    fun `Test equals`() {
        val stringValue1 = StringField("test")
        val stringValue2 = StringField("test")
        val stringValue3 = StringField("anotherTest")
        val compositeAttribute1 = CompositeAttribute("name", stringValue1)
        val compositeAttribute2 = CompositeAttribute("name", stringValue2)
        val compositeAttribute3 = CompositeAttribute("name", stringValue3)

        assertEquals(compositeAttribute1, compositeAttribute2)
        assertNotEquals(compositeAttribute1, compositeAttribute3)
    }
}
