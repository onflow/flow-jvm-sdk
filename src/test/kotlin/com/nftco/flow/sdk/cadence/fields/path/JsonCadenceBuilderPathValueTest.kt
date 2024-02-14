package com.nftco.flow.sdk.cadence.fields.path

import com.nftco.flow.sdk.cadence.PathValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class JsonCadenceBuilderPathValueTest {
    @Test
    fun `Test hashCode`() {
        val pathValue1 = PathValue("domain1", "identifier1")
        val pathValue2 = PathValue("domain1", "identifier1")
        val pathValue3 = PathValue("domain2", "identifier2")

        assertEquals(pathValue1.hashCode(), pathValue2.hashCode())
        assertNotEquals(pathValue1.hashCode(), pathValue3.hashCode())
    }

    @Test
    fun `Test equals`() {
        val pathValue1 = PathValue("domain1", "identifier1")
        val pathValue2 = PathValue("domain1", "identifier1")
        val pathValue3 = PathValue("domain2", "identifier2")

        assertEquals(pathValue1, pathValue2)
        assertNotEquals(pathValue1, pathValue3)
    }
}
