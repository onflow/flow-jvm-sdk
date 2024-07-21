package org.onflow.flow.sdk.cadence.fields

import org.onflow.flow.sdk.cadence.BooleanField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class JsonCadenceBuilderBooleanFieldTest {
    @Test
    fun `Test equality for BooleanField`() {
        val bool1 = BooleanField(true)
        val bool2 = BooleanField(true)
        val bool3 = BooleanField(false)

        assertEquals(bool1, bool2)
        assertEquals(bool1.hashCode(), bool2.hashCode())

        assertNotEquals(bool1, bool3)
        assertNotEquals(bool1.hashCode(), bool3.hashCode())
    }

    @Test
    fun `Test hashCode for BooleanField with true value`() {
        val boolField1 = BooleanField(true)
        val boolField2 = BooleanField(true)

        assertEquals(boolField1.hashCode(), boolField2.hashCode())
    }

    @Test
    fun `Test hashCode for BooleanField with false value`() {
        val boolField1 = BooleanField(false)
        val boolField2 = BooleanField(false)

        assertEquals(boolField1.hashCode(), boolField2.hashCode())
    }

    @Test
    fun `Test hashCode for different BooleanField values`() {
        val boolField1 = BooleanField(true)
        val boolField2 = BooleanField(false)

        assert(boolField1.hashCode() != boolField2.hashCode())
    }

    @Test
    fun `Test decoding of BooleanField with true value`() {
        val boolField = BooleanField(true)
        assertEquals(true, boolField.decodeToAny())
    }

    @Test
    fun `Test decoding of BooleanField with false value`() {
        val boolField = BooleanField(false)
        assertEquals(false, boolField.decodeToAny())
    }
}
