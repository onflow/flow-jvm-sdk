package com.nftco.flow.sdk.cadence.fields.composite

import com.nftco.flow.sdk.cadence.CompositeAttribute
import com.nftco.flow.sdk.cadence.CompositeValue
import com.nftco.flow.sdk.cadence.IntNumberField
import com.nftco.flow.sdk.cadence.StringField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JsonCadenceBuilderCompositeValueTest {
    @Test
    fun `Test getField`() {
        val stringValue = StringField("test")
        val compositeAttribute = CompositeAttribute("name", stringValue)
        val compositeValue = CompositeValue("id", arrayOf(compositeAttribute))

        assertEquals(stringValue, compositeValue.getField<StringField>("name"))
        assertNull(compositeValue.getField<IntNumberField>("nonExistentField"))
    }

    @Test
    fun `Test getRequiredField`() {
        val stringValue = StringField("test")
        val compositeAttribute = CompositeAttribute("name", stringValue)
        val compositeValue = CompositeValue("id", arrayOf(compositeAttribute))

        assertEquals(stringValue, compositeValue.getRequiredField<StringField>("name"))
        assertThrows<IllegalStateException> {
            compositeValue.getRequiredField<IntNumberField>("nonExistentField")
        }
    }

    @Test
    fun `Test operator get`() {
        val stringValue = StringField("test")
        val compositeAttribute = CompositeAttribute("name", stringValue)
        val compositeValue = CompositeValue("id", arrayOf(compositeAttribute))

        assertEquals(stringValue, compositeValue["name"])
        assertNull(compositeValue[IntNumberField::class.java.simpleName])
        assertNull(compositeValue["nonExistentField"])
    }

    @Test
    fun `Test operator contains`() {
        val stringValue = StringField("test")
        val compositeAttribute = CompositeAttribute("name", stringValue)
        val compositeValue = CompositeValue("id", arrayOf(compositeAttribute))

        assertEquals(true, "name" in compositeValue)
        assertEquals(false, "nonExistentField" in compositeValue)
    }
}
