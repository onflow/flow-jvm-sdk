package com.nftco.flow.sdk.cadence.fields

import com.nftco.flow.sdk.cadence.ArrayField
import com.nftco.flow.sdk.cadence.Field
import com.nftco.flow.sdk.cadence.IntNumberField
import com.nftco.flow.sdk.cadence.StringField
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class JsonCadenceBuilderArrayFieldTest {
    @Test
    fun `Test equality for ArrayField`() {
        val array1 = ArrayField(arrayOf(StringField("abc"), IntNumberField("42")))
        val array2 = ArrayField(arrayOf(StringField("abc"), IntNumberField("42")))

        assertEquals(array1, array2)
        assertEquals(array1.hashCode(), array2.hashCode())
    }

    @Test
    fun `Test inequality for different ArrayField values`() {
        val array1 = ArrayField(arrayOf(StringField("abc"), IntNumberField("42")))
        val array2 = ArrayField(arrayOf(StringField("def"), IntNumberField("42")))

        assertNotEquals(array1, array2)
    }

    @Test
    fun `Test decoding of ArrayField`() {
        val arrayField = ArrayField(arrayOf(StringField("abc"), IntNumberField("42")))
        val decodedArray = arrayField.decodeToAny()

        assertEquals(listOf("abc", 42), decodedArray)
    }

    @Test
    fun `Test decoding of empty ArrayField`() {
        val emptyArrayField = ArrayField(emptyArray())
        val decodedArray = emptyArrayField.decodeToAny()

        assertEquals(emptyList<Any>(), decodedArray)
    }

    @Test
    fun `Test hash code for ArrayField`() {
        val array1 = ArrayField(arrayOf(StringField("abc"), IntNumberField("42")))
        val array2 = ArrayField(arrayOf(StringField("abc"), IntNumberField("42")))

        assertEquals(array1.hashCode(), array2.hashCode())
    }

    @Test
    fun `Test hash code for different ArrayField values`() {
        val array1 = ArrayField(arrayOf(StringField("abc"), IntNumberField("42")))
        val array2 = ArrayField(arrayOf(StringField("def"), IntNumberField("42")))

        assertNotEquals(array1.hashCode(), array2.hashCode())
    }

    @Test
    fun `Test decoding of ArrayField with an element of unsupported type`() {
        val arrayField = ArrayField(arrayOf(StringField("abc"), UnsupportedField()))

        val expectedMessage = " Can't find right class "

        assertThatThrownBy { arrayField.decodeToAny() }
            .isInstanceOf(Exception::class.java)
            .hasMessage(expectedMessage)
    }

    private class UnsupportedField : Field<String>("UnsupportedType", "value")
}
