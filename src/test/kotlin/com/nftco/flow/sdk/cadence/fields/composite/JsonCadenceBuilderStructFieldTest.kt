package com.nftco.flow.sdk.cadence.fields.composite

import com.nftco.flow.sdk.cadence.CompositeValue
import com.nftco.flow.sdk.cadence.StructField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class JsonCadenceBuilderStructFieldTest {
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
        val compositeValue = CompositeValue("structId", arrayOf())
        val structField = StructField(compositeValue)

        val decodedValue = structField.decodeToAny()
        assertEquals(compositeValue, decodedValue)
    }
}
