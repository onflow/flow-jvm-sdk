package org.flowfoundation.flow.sdk.cadence.fields.path

import org.flowfoundation.flow.sdk.cadence.PathField
import org.flowfoundation.flow.sdk.cadence.PathValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class JsonCadenceBuilderPathFieldTest {
    @Test
    fun `Test hashCode`() {
        val pathValue1 = PathValue("domain1", "identifier1")
        val pathValue2 = PathValue("domain1", "identifier1")
        val pathField1 = PathField(pathValue1)
        val pathField2 = PathField(pathValue2)

        assertEquals(pathField1.hashCode(), pathField2.hashCode())
    }

    @Test
    fun `Test hashCode with different values`() {
        val pathValue1 = PathValue("domain1", "identifier1")
        val pathValue2 = PathValue("domain2", "identifier2")
        val pathField1 = PathField(pathValue1)
        val pathField2 = PathField(pathValue2)

        assertNotEquals(pathField1.hashCode(), pathField2.hashCode())
    }

    @Test
    fun `Test decoding PathField`() {
        val pathValue = PathValue("domain", "identifier")
        val pathField = PathField(pathValue)
        val decodedValue = pathField.decodeToAny()

        assertEquals(pathValue, decodedValue)
    }
}
