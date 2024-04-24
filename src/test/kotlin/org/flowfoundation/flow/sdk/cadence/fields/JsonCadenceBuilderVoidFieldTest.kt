package org.onflow.flow.sdk.cadence.fields

import org.onflow.flow.sdk.cadence.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JsonCadenceBuilderVoidFieldTest {
    @Test
    fun `Test equality of VoidField instances`() {
        val voidField1 = VoidField()
        val voidField2 = VoidField()

        assertEquals(voidField1, voidField2)
    }

    @Test
    fun `Test hashing of VoidField instances`() {
        val voidField1 = VoidField()
        val voidField2 = VoidField()

        assertEquals(voidField1.hashCode(), voidField2.hashCode())
    }

    @Test
    fun `Test equality with other Field types`() {
        val voidField = VoidField()
        val stringField = StringField("test")

        assertNotEquals(voidField, stringField)
    }

    @Test
    fun `Test decoding VoidField returns null`() {
        val voidField = VoidField()

        val decodedValue: Any? = voidField.decodeToAny()

        assertThat(decodedValue).isNull()
    }
}
