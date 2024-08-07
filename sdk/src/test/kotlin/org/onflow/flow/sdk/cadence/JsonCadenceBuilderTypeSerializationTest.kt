package org.onflow.flow.sdk.cadence

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import org.onflow.flow.common.test.FlowTestUtil

class JsonCadenceBuilderTypeSerializationTest {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    // Simple types
    @Test
    fun `Test simple type serialization and deserialization`() {
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_ANY))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_ANYSTRUCT))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_ANYRESOURCE))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_TYPE))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_VOID))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_NEVER))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_BOOLEAN))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_STRING))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_CHARACTER))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_BYTES))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_NUMBER))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_SIGNEDNUMBER))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_INTEGER))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_SIGNEDINTEGER))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_FIXEDPOINT))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_SIGNEDFIXEDPOINT))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_INT))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_UINT))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_INT8))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_UINT8))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_INT16))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_UINT16))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_INT32))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_UINT32))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_INT64))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_UINT64))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_INT128))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_UINT128))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_INT256))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_UINT256))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_WORD8))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_WORD16))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_WORD32))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_WORD64))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_FIX64))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_UFIX64))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_ARRAY))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_ADDRESS))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_PATH))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_CAPABILITYPATH))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_STORAGEPATH))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_PUBLICPATH))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_PRIVATEPATH))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_AUTHACCOUNT))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_PUBLICACCOUNT))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_AUTHACCOUNT_KEYS))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_PUBLICACCOUNT_KEYS))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_AUTHACCOUNT_CONTRACTS))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_PUBLICACCOUNT_CONTRACTS))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_DEPLOYEDCONTRACT))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_ACCOUNTKEY))
        assertSimpleTypeSerializationAndDeserialization(SimpleType(TYPE_BLOCK))
    }

    private fun assertSimpleTypeSerializationAndDeserialization(originalType: CadenceType) {
        val jsonString = objectMapper.writeValueAsString(originalType)
        val deserializedType = objectMapper.readValue(jsonString, CadenceType::class.java)

        assertEquals(originalType.kind, deserializedType.kind, "Expected kind: ${originalType.kind}, Actual kind: ${deserializedType.kind}")

        assertEquals(objectMapper.writeValueAsString(originalType), objectMapper.writeValueAsString(deserializedType), "JSON strings do not match for type: ${originalType.kind}")
    }

    // Complex types
    @Test
    fun `Test complex type serialization and deserialization`() {
        assertComplexTypeSerializationAndDeserialization(OptionalType(createStringType()))
        assertComplexTypeSerializationAndDeserializationOptionalType(OptionalType(createStringType()))

        assertComplexTypeSerializationAndDeserialization(VariableSizedArrayType(createStringType()))
        assertComplexTypeSerializationAndDeserializationVariableSizedArrayType(VariableSizedArrayType(createStringType()))

        assertComplexTypeSerializationAndDeserialization(ConstantSizedArrayType(createStringType()))
        assertComplexTypeSerializationAndDeserializationConstantSizedArrayType(ConstantSizedArrayType(createStringType()))

        assertComplexTypeSerializationAndDeserialization(DictionaryType(createStringType(), createIntType()))
        assertComplexTypeSerializationAndDeserializationDictionaryType(DictionaryType(createStringType(), createIntType()))

        assertComplexTypeSerializationAndDeserialization(FunctionType("typeID", arrayOf(), createStringType()))
        assertComplexTypeSerializationAndDeserializationFunctionType(FunctionType("typeID", arrayOf(), createStringType()))

        assertComplexTypeSerializationAndDeserialization(ReferenceType(null, true, createStringType()))
        assertComplexTypeSerializationAndDeserializationReferenceType(ReferenceType(null, true, createStringType()))

        assertComplexTypeSerializationAndDeserialization(RestrictionType("typeID", createStringType(), arrayOf(createStringType())))
        assertComplexTypeSerializationAndDeserializationRestrictionType(RestrictionType("typeID", createStringType(), arrayOf(createStringType())))

        assertComplexTypeSerializationAndDeserialization(CapabilityType(createStringType()))
        assertComplexTypeSerializationAndDeserializationCapabilityType(CapabilityType(createStringType()))

        assertComplexTypeSerializationAndDeserialization(EnumType(createStringType(), "typeID", arrayOf(), arrayOf()))
        assertComplexTypeSerializationAndDeserializationEnumType(EnumType(createStringType(), "typeID", arrayOf(), arrayOf()))
    }

    private fun createStringType(): CadenceType {
        // Replace this with the appropriate way to create a StringType instance in your code
        return SimpleType(TYPE_STRING)
    }

    private fun createIntType(): CadenceType {
        // Replace this with the appropriate way to create an IntType instance in your code
        return SimpleType(TYPE_INT)
    }

    private fun assertComplexTypeSerializationAndDeserialization(originalType: CadenceType) {
        val jsonString = objectMapper.writeValueAsString(originalType)
        val deserializedType = objectMapper.readValue(jsonString, CadenceType::class.java)

        assertEquals(originalType.kind, deserializedType.kind, "Expected kind: ${originalType.kind}, Actual kind: ${deserializedType.kind}")

        assertEquals(objectMapper.writeValueAsString(originalType), objectMapper.writeValueAsString(deserializedType), "JSON strings do not match for type: ${originalType.kind}")
    }

    private fun assertComplexTypeSerializationAndDeserializationOptionalType(originalType: OptionalType) {
        val jsonString = objectMapper.writeValueAsString(originalType)
        val deserializedType = objectMapper.readValue(jsonString, OptionalType::class.java)

        assertEquals(objectMapper.writeValueAsString(originalType.type), objectMapper.writeValueAsString(deserializedType.type), "Expected type: ${originalType.type}, Actual type: ${deserializedType.type}")
    }

    private fun assertComplexTypeSerializationAndDeserializationVariableSizedArrayType(originalType: VariableSizedArrayType) {
        val jsonString = objectMapper.writeValueAsString(originalType)
        val deserializedType = objectMapper.readValue(jsonString, VariableSizedArrayType::class.java)

        assertEquals(objectMapper.writeValueAsString(originalType.type), objectMapper.writeValueAsString(deserializedType.type), "Expected type: ${originalType.type}, Actual type: ${deserializedType.type}")
    }

    private fun assertComplexTypeSerializationAndDeserializationConstantSizedArrayType(originalType: ConstantSizedArrayType) {
        val jsonString = objectMapper.writeValueAsString(originalType)
        val deserializedType = objectMapper.readValue(jsonString, ConstantSizedArrayType::class.java)

        assertEquals(objectMapper.writeValueAsString(originalType.type), objectMapper.writeValueAsString(deserializedType.type), "Expected type: ${originalType.type}, Actual type: ${deserializedType.type}")
    }

    private fun assertComplexTypeSerializationAndDeserializationDictionaryType(originalType: DictionaryType) {
        val jsonString = objectMapper.writeValueAsString(originalType)
        val deserializedType = objectMapper.readValue(jsonString, DictionaryType::class.java)

        assertEquals(objectMapper.writeValueAsString(originalType.key), objectMapper.writeValueAsString(deserializedType.key), "Expected key type: ${originalType.key}, Actual key type: ${deserializedType.key}")
        assertEquals(objectMapper.writeValueAsString(originalType.value), objectMapper.writeValueAsString(deserializedType.value), "Expected value type: ${originalType.value}, Actual value type: ${deserializedType.value}")
    }

    private fun assertComplexTypeSerializationAndDeserializationReferenceType(originalType: ReferenceType) {
        val jsonString = objectMapper.writeValueAsString(originalType)
        val deserializedType = objectMapper.readValue(jsonString, ReferenceType::class.java)

        assertEquals(originalType.typeID, deserializedType.typeID, "Expected typeID: ${originalType.typeID}, Actual typeID: ${deserializedType.typeID}")
        assertEquals(objectMapper.writeValueAsString(originalType.type), objectMapper.writeValueAsString(deserializedType.type), "Expected referenced type: ${originalType.type}, Actual referenced type: ${deserializedType.type}")
    }

    private fun assertComplexTypeSerializationAndDeserializationRestrictionType(originalType: RestrictionType) {
        val jsonString = objectMapper.writeValueAsString(originalType)
        val deserializedType = objectMapper.readValue(jsonString, RestrictionType::class.java)

        assertEquals(objectMapper.writeValueAsString(originalType.type), objectMapper.writeValueAsString(deserializedType.type), "Expected restricted type: ${originalType.type}, Actual restricted type: ${deserializedType.type}")
        assertEquals(objectMapper.writeValueAsString(originalType.restrictions), objectMapper.writeValueAsString(deserializedType.restrictions), "Expected restrictions: ${originalType.restrictions}, Actual restrictions: ${deserializedType.restrictions}")
    }

    private fun assertComplexTypeSerializationAndDeserializationCapabilityType(originalType: CapabilityType) {
        val jsonString = objectMapper.writeValueAsString(originalType)
        val deserializedType = objectMapper.readValue(jsonString, CapabilityType::class.java)

        assertEquals(objectMapper.writeValueAsString(originalType.type), objectMapper.writeValueAsString(deserializedType.type), "Expected referenced type: ${originalType.type}, Actual referenced type: ${deserializedType.type}")
    }

    private fun assertComplexTypeSerializationAndDeserializationEnumType(originalType: EnumType) {
        val jsonString = objectMapper.writeValueAsString(originalType)
        val deserializedType = objectMapper.readValue(jsonString, EnumType::class.java)

        assertEquals(objectMapper.writeValueAsString(originalType.type), objectMapper.writeValueAsString(deserializedType.type), "Expected enum type: ${originalType.type}, Actual enum type: ${deserializedType.type}")
        assertEquals(objectMapper.writeValueAsString(originalType.typeID), objectMapper.writeValueAsString(deserializedType.typeID), "Expected typeID: ${originalType.typeID}, Actual typeID: ${deserializedType.typeID}")
        assertEquals(objectMapper.writeValueAsString(originalType.initializers), objectMapper.writeValueAsString(deserializedType.initializers), "Expected initializers: ${originalType.initializers}, Actual initializers: ${deserializedType.initializers}")
        assertEquals(objectMapper.writeValueAsString(originalType.fields), objectMapper.writeValueAsString(deserializedType.fields), "Expected fields: ${originalType.fields}, Actual fields: ${deserializedType.fields}")
    }

    private fun assertComplexTypeSerializationAndDeserializationFunctionType(originalType: FunctionType) {
        val jsonString = objectMapper.writeValueAsString(originalType)
        val deserializedType = objectMapper.readValue(jsonString, FunctionType::class.java)

        assertEquals(
            objectMapper.writeValueAsString(originalType.parameters),
            objectMapper.writeValueAsString(deserializedType.parameters),
            "Expected parameters: ${originalType.parameters}, Actual parameters: ${deserializedType.parameters}"
        )
        assertEquals(
            objectMapper.writeValueAsString(originalType.`return`),
            objectMapper.writeValueAsString(deserializedType.`return`),
            "Expected return type: ${originalType.`return`}, Actual return type: ${deserializedType.`return`}"
        )
    }

    // Composite types
    @Test
    fun `Test composite type serialization and deserialization`() {
        val structType = CompositeType(
            TYPE_STRUCT,
            "StructType",
            "id",
            emptyArray(),
            arrayOf(FieldType("field", SimpleType(TYPE_STRING)))
        )
        assertCompositeTypeSerializationAndDeserialization(structType)

        val resourceType = CompositeType(
            TYPE_RESOURCE,
            "ResourceType",
            "id",
            emptyArray(),
            arrayOf(FieldType("field", SimpleType(TYPE_STRING)))
        )
        assertCompositeTypeSerializationAndDeserialization(resourceType)

        val eventType = CompositeType(
            TYPE_EVENT,
            "EventType",
            "id",
            emptyArray(),
            arrayOf(FieldType("field", SimpleType(TYPE_STRING)))
        )
        assertCompositeTypeSerializationAndDeserialization(eventType)

        val contractType = CompositeType(
            TYPE_CONTRACT,
            "ContractType",
            "id",
            emptyArray(),
            arrayOf(FieldType("field", SimpleType(TYPE_STRING)))
        )
        assertCompositeTypeSerializationAndDeserialization(contractType)

        val enumType = CompositeType(
            TYPE_ENUM,
            "EnumType",
            "id",
            emptyArray(), // No initializers
            arrayOf(FieldType("field", SimpleType(TYPE_STRING)))
        )
        assertCompositeTypeSerializationAndDeserialization(enumType)

        val structInterfaceType = CompositeType(
            TYPE_STRUCT_INTERFACE,
            "StructInterfaceType",
            "id",
            emptyArray(), // No initializers
            arrayOf(FieldType("field", SimpleType(TYPE_STRING)))
        )
        assertCompositeTypeSerializationAndDeserialization(structInterfaceType)

        val resourceInterfaceType = CompositeType(
            TYPE_RESOURCE_INTERFACE,
            "ResourceInterfaceType",
            "id",
            emptyArray(), // No initializers
            arrayOf(FieldType("field", SimpleType(TYPE_STRING)))
        )
        assertCompositeTypeSerializationAndDeserialization(resourceInterfaceType)

        val contractInterfaceType = CompositeType(
            TYPE_CONTRACT_INTERFACE,
            "ContractInterfaceType",
            "id",
            emptyArray(), // No initializers
            arrayOf(FieldType("field", SimpleType(TYPE_STRING)))
        )
        assertCompositeTypeSerializationAndDeserialization(contractInterfaceType)
    }

    private fun assertCompositeTypeSerializationAndDeserialization(originalType: CompositeType) {
        val jsonString = objectMapper.writeValueAsString(originalType)
        val deserializedType = objectMapper.readValue(jsonString, CompositeType::class.java)

        assertEquals(
            objectMapper.writeValueAsString(originalType.type),
            objectMapper.writeValueAsString(deserializedType.type),
            "Expected type: ${originalType.type}, Actual type: ${deserializedType.type}"
        )
        assertEquals(
            objectMapper.writeValueAsString(originalType.typeID),
            objectMapper.writeValueAsString(deserializedType.typeID),
            "Expected typeID: ${originalType.typeID}, Actual typeID: ${deserializedType.typeID}"
        )
        assertEquals(
            objectMapper.writeValueAsString(originalType.initializers),
            objectMapper.writeValueAsString(deserializedType.initializers),
            "Expected initializers: ${originalType.initializers}, Actual initializers: ${deserializedType.initializers}"
        )
        assertEquals(
            objectMapper.writeValueAsString(originalType.fields),
            objectMapper.writeValueAsString(deserializedType.fields),
            "Expected fields: ${originalType.fields}, Actual fields: ${deserializedType.fields}"
        )
    }

    // Exception handling
    @Test
    fun `test decode with invalid JSON`() {
        val invalidJson = String(FlowTestUtil.loadScript("json/invalid.json"), StandardCharsets.UTF_8)
        assertThrows(Exception::class.java) {
            objectMapper.readValue(invalidJson, CadenceType::class.java)
        }
    }

    @Test
    fun `test decode with missing kind field in generic Cadence type`() {
        val invalidJson = String(FlowTestUtil.loadScript("json/missing_kind.json"), StandardCharsets.UTF_8)
        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJson, CadenceType::class.java)
        }
    }

    @Test
    fun `test decode with missing kind field in simple type`() {
        val invalidJson = String(FlowTestUtil.loadScript("json/missing_kind.json"), StandardCharsets.UTF_8)
        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJson, SimpleType::class.java)
        }
    }

    @Test
    fun `test decode with missing type field in complex type`() {
        val invalidJsonOptional = String(FlowTestUtil.loadScript("json/invalid_optional.json"), StandardCharsets.UTF_8)

        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJsonOptional, OptionalType::class.java)
        }

        val invalidJsonVariableSizedArray = String(FlowTestUtil.loadScript("json/invalid_variable_sized_array.json"), StandardCharsets.UTF_8)

        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJsonVariableSizedArray, VariableSizedArrayType::class.java)
        }

        val invalidJsonConstantSizedArray = String(FlowTestUtil.loadScript("json/invalid_constant_sized_array.json"), StandardCharsets.UTF_8)

        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJsonConstantSizedArray, ConstantSizedArrayType::class.java)
        }

        // Missing value field
        val invalidJsonDictionary = String(FlowTestUtil.loadScript("json/invalid_dictionary.json"), StandardCharsets.UTF_8)

        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJsonDictionary, DictionaryType::class.java)
        }

        // Missing typeID field
        val invalidJsonFunction = String(FlowTestUtil.loadScript("json/invalid_function.json"), StandardCharsets.UTF_8)

        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJsonFunction, FunctionType::class.java)
        }

        val invalidJsonReference = String(FlowTestUtil.loadScript("json/invalid_reference.json"), StandardCharsets.UTF_8)

        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJsonReference, ReferenceType::class.java)
        }

        val invalidJsonRestriction = String(FlowTestUtil.loadScript("json/invalid_restriction.json"), StandardCharsets.UTF_8)

        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJsonRestriction, RestrictionType::class.java)
        }

        val invalidJsonCapability = String(FlowTestUtil.loadScript("json/invalid_capability.json"), StandardCharsets.UTF_8)

        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJsonCapability, CapabilityType::class.java)
        }

        val invalidJsonEnum = String(FlowTestUtil.loadScript("json/invalid_enum.json"), StandardCharsets.UTF_8)

        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJsonEnum, EnumType::class.java)
        }
    }

    @Test
    fun `test decode with partial Cadence type and missing type field`() {
        val invalidJson = String(FlowTestUtil.loadScript("json/missing_type.json"), StandardCharsets.UTF_8)
        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJson, PartialCadenceType::class.java)
        }
    }

    @Test
    fun `test decode with missing type field in composite type`() {
        val invalidJson = String(FlowTestUtil.loadScript("json/missing_type_composite.json"), StandardCharsets.UTF_8)
        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJson, CompositeType::class.java)
        }
    }

    @Test
    fun `test decode with missing typeID field in composite type`() {
        val invalidJson = String(FlowTestUtil.loadScript("json/missing_type_id_composite.json"), StandardCharsets.UTF_8)
        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJson, CompositeType::class.java)
        }
    }

    @Test
    fun `test decode with unknown CadenceType kind`() {
        val invalidJson = String(FlowTestUtil.loadScript("json/unknown_kind.json"), StandardCharsets.UTF_8)
        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJson, CadenceType::class.java)
        }
    }

    @Test
    fun `test decode with unknown CadenceType kind 2`() {
        val invalidJson = String(FlowTestUtil.loadScript("json/unknown_kind_2.json"), StandardCharsets.UTF_8)
        assertThrows(MismatchedInputException::class.java) {
            objectMapper.readValue(invalidJson, CadenceType::class.java)
        }
    }
}
