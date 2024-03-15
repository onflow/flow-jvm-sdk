package org.flowfoundation.flow.sdk.cadence.fields.composite

import org.flowfoundation.flow.sdk.cadence.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class JsonCadenceBuilderContractFieldTest {
    @Test
    fun `Test ContractField type and value properties`() {
        val fieldValue = CompositeValue("contractId", emptyArray())
        val contractField = ContractField(fieldValue)

        assertEquals(TYPE_CONTRACT, contractField.type)
        assertEquals(fieldValue, contractField.value)
    }

    @Test
    fun `Test hashCode`() {
        val compositeValue1 = CompositeValue("id1", arrayOf())
        val contractField1 = ContractField(compositeValue1)

        val compositeValue2 = CompositeValue("id1", arrayOf())
        val contractField2 = ContractField(compositeValue2)

        assertEquals(contractField1.hashCode(), contractField2.hashCode())
    }

    @Test
    fun `Test equals`() {
        val compositeValue1 = CompositeValue("id1", arrayOf())
        val contractField1 = ContractField(compositeValue1)

        val compositeValue2 = CompositeValue("id1", arrayOf())
        val contractField2 = ContractField(compositeValue2)

        val compositeValue3 = CompositeValue("id2", arrayOf())
        val contractField3 = ContractField(compositeValue3)

        assertEquals(contractField1, contractField2)
        assertNotEquals(contractField1, contractField3)
    }

    @Test
    fun `Test decoding ContractField`() {
        val compositeValue = CompositeValue("contractId", arrayOf(CompositeAttribute("id", StringField("contract"))))
        val contractField = ContractField(compositeValue)

        val decodedValue = contractField.decodeToAny()
        assertEquals(mapOf("id" to "contractId", "fields" to compositeValue.fields), decodedValue)
    }
}
