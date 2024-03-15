package org.flowfoundation.flow.sdk.cadence

// This files contains types for the JSON-Cadence Data Interchange Format
import java.util.Objects
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.flowfoundation.flow.sdk.Flow
import org.flowfoundation.flow.sdk.FlowAddress
import org.flowfoundation.flow.sdk.bytesToHex
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.KClass

// https://docs.onflow.org/cadence/json-cadence-spec/#types

// simple types
const val TYPE_ANY = "Any"
const val TYPE_ANYSTRUCT = "AnyStruct"
const val TYPE_ANYRESOURCE = "AnyResource"
const val TYPE_TYPE = "Type"
const val TYPE_VOID = "Void"
const val TYPE_NEVER = "Never"
const val TYPE_BOOLEAN = "Bool"
const val TYPE_STRING = "String"
const val TYPE_CHARACTER = "Character"
const val TYPE_BYTES = "Bytes"
const val TYPE_NUMBER = "Number"
const val TYPE_SIGNEDNUMBER = "SignedNumber"
const val TYPE_INTEGER = "Integer"
const val TYPE_SIGNEDINTEGER = "SignedInteger"
const val TYPE_FIXEDPOINT = "FixedPoint"
const val TYPE_SIGNEDFIXEDPOINT = "SignedFixedPoint"
const val TYPE_INT = "Int"
const val TYPE_UINT = "UInt"
const val TYPE_INT8 = "Int8"
const val TYPE_UINT8 = "UInt8"
const val TYPE_INT16 = "Int16"
const val TYPE_UINT16 = "UInt16"
const val TYPE_INT32 = "Int32"
const val TYPE_UINT32 = "UInt32"
const val TYPE_INT64 = "Int64"
const val TYPE_UINT64 = "UInt64"
const val TYPE_INT128 = "Int128"
const val TYPE_UINT128 = "UInt128"
const val TYPE_INT256 = "Int256"
const val TYPE_UINT256 = "UInt256"
const val TYPE_WORD8 = "Word8"
const val TYPE_WORD16 = "Word16"
const val TYPE_WORD32 = "Word32"
const val TYPE_WORD64 = "Word64"
const val TYPE_FIX64 = "Fix64"
const val TYPE_UFIX64 = "UFix64"
const val TYPE_ARRAY = "Array"
const val TYPE_ADDRESS = "Address"
const val TYPE_PATH = "Path"
const val TYPE_CAPABILITYPATH = "CapabilityPath"
const val TYPE_STORAGEPATH = "StoragePath"
const val TYPE_PUBLICPATH = "PublicPath"
const val TYPE_PRIVATEPATH = "PrivatePath"
const val TYPE_AUTHACCOUNT = "AuthAccount"
const val TYPE_PUBLICACCOUNT = "PublicAccount"
const val TYPE_AUTHACCOUNT_KEYS = "AuthAccount.Keys"
const val TYPE_PUBLICACCOUNT_KEYS = "PublicAccount.Keys"
const val TYPE_AUTHACCOUNT_CONTRACTS = "AuthAccount.Contracts"
const val TYPE_PUBLICACCOUNT_CONTRACTS = "PublicAccount.Contracts"
const val TYPE_DEPLOYEDCONTRACT = "DeployedContract"
const val TYPE_ACCOUNTKEY = "AccountKey"
const val TYPE_BLOCK = "Block"

// complex type
const val TYPE_OPTIONAL = "Optional"
const val TYPE_DICTIONARY = "Dictionary"
const val TYPE_VARIABLE_SIZED_ARRAY = "VariableSizedArray"
const val TYPE_CONSTANT_SIZED_ARRAY = "ConstantSizedArray"
const val TYPE_CAPABILITY = "Capability"
const val TYPE_ENUM = "Enum"
const val TYPE_FUNCTION = "Function"
const val TYPE_REFERENCE = "Reference"
const val TYPE_RESTRICTION = "Restriction"

// composite types
const val TYPE_STRUCT = "Struct"
const val TYPE_RESOURCE = "Resource"
const val TYPE_EVENT = "Event"
const val TYPE_CONTRACT = "Contract"
const val TYPE_STRUCT_INTERFACE = "StructInterface"
const val TYPE_RESOURCE_INTERFACE = "ResourceInterface"
const val TYPE_CONTRACT_INTERFACE = "ContractInterface"

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    value = [
        Type(value = VoidField::class, name = TYPE_VOID),
        Type(value = OptionalField::class, name = TYPE_OPTIONAL),
        Type(value = BooleanField::class, name = TYPE_BOOLEAN),
        Type(value = StringField::class, name = TYPE_STRING),
        Type(value = IntNumberField::class, name = TYPE_INT),
        Type(value = UIntNumberField::class, name = TYPE_UINT),
        Type(value = Int8NumberField::class, name = TYPE_INT8),
        Type(value = UInt8NumberField::class, name = TYPE_UINT8),
        Type(value = Int16NumberField::class, name = TYPE_INT16),
        Type(value = UInt16NumberField::class, name = TYPE_UINT16),
        Type(value = Int32NumberField::class, name = TYPE_INT32),
        Type(value = UInt32NumberField::class, name = TYPE_UINT32),
        Type(value = Int64NumberField::class, name = TYPE_INT64),
        Type(value = UInt64NumberField::class, name = TYPE_UINT64),
        Type(value = Int128NumberField::class, name = TYPE_INT128),
        Type(value = UInt128NumberField::class, name = TYPE_UINT128),
        Type(value = Int256NumberField::class, name = TYPE_INT256),
        Type(value = UInt256NumberField::class, name = TYPE_UINT256),
        Type(value = Word8NumberField::class, name = TYPE_WORD8),
        Type(value = Word16NumberField::class, name = TYPE_WORD16),
        Type(value = Word32NumberField::class, name = TYPE_WORD32),
        Type(value = Word64NumberField::class, name = TYPE_WORD64),
        Type(value = Fix64NumberField::class, name = TYPE_FIX64),
        Type(value = UFix64NumberField::class, name = TYPE_UFIX64),
        Type(value = ArrayField::class, name = TYPE_ARRAY),
        Type(value = DictionaryField::class, name = TYPE_DICTIONARY),
        Type(value = AddressField::class, name = TYPE_ADDRESS),
        Type(value = PathField::class, name = TYPE_PATH),
        Type(value = CapabilityField::class, name = TYPE_CAPABILITY),
        Type(value = StructField::class, name = TYPE_STRUCT),
        Type(value = ResourceField::class, name = TYPE_RESOURCE),
        Type(value = EventField::class, name = TYPE_EVENT),
        Type(value = ContractField::class, name = TYPE_CONTRACT),
        Type(value = EnumField::class, name = TYPE_ENUM),
        Type(value = TypeField::class, name = TYPE_TYPE)
    ]
)

abstract class Field<T> constructor(
    val type: String,
    val value: T?
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Field<*>) return false
        // TODO: something better than this
        return String(Flow.encodeJsonCadence(this)) == String(Flow.encodeJsonCadence(other))
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }

    @kotlin.jvm.Throws
    fun decodeToAny(): Any? {
        return when (this) {
            is StringField -> {
                value
            }

            is BooleanField -> {
                value
            }

            is VoidField -> {
                null
            }

            is OptionalField -> {
                value?.decodeToAny()
            }

            is IntNumberField -> {
                toInt()
            }

            is UIntNumberField -> {
                toUInt()
            }

            is Int8NumberField -> {
                toInt()
            }

            is UInt8NumberField -> {
                toUInt()
            }

            is Int16NumberField -> {
                toInt()
            }

            is UInt16NumberField -> {
                toUInt()
            }

            is Int32NumberField -> {
                toInt()
            }

            is UInt32NumberField -> {
                toUInt()
            }

            is Int64NumberField -> {
                toLong()
            }

            is UInt64NumberField -> {
                toULong()
            }

            is Int128NumberField -> {
                toBigInteger()
            }

            is UInt128NumberField -> {
                toBigInteger()
            }

            is Int256NumberField -> {
                toBigInteger()
            }

            is UInt256NumberField -> {
                toBigInteger()
            }

            is Word8NumberField -> {
                toUInt()
            }

            is Word16NumberField -> {
                toUInt()
            }

            is Word32NumberField -> {
                toUInt()
            }

            is Word64NumberField -> {
                toUInt()
            }

            is Fix64NumberField -> {
                toDouble()
            }

            is UFix64NumberField -> {
                toDouble()
            }

            is ArrayField -> {
                value?.map { it.decodeToAny() }
            }

            is AddressField -> {
                value?.let { FlowAddress(it) }
            }

            // CompositeValue
            is StructField -> {
                value?.toMap()
            }

            is EnumField -> {
                value?.toMap()
            }

            is ResourceField -> {
                value?.toMap()
            }

            is EventField -> {
                value?.toMap()
            }

            is ContractField -> {
                value?.let { toMap(it) }
            }

            is DictionaryField -> {
                this.value?.associate {
                    it.key.decodeToAny() to it.value.decodeToAny()
                }
            }

            is CapabilityField -> {
                value?.let { toMap(it) }
            }

            is PathField -> value?.let {
                PathValue(it.domain, it.identifier)
            }

            // TODO: Handle type decode
            is TypeField -> {
                value?.let { toMap(it) }
            }

            else -> {
                throw Exception(" Can't find right class ")
            }
        }
    }

    @kotlin.jvm.Throws
    inline fun <reified T> decode(): T {
        val jsonElement = decodeToAny().toJsonElement()
        return Json.decodeFromJsonElement(jsonElement)
    }
}

fun CompositeValue.toMap(): Map<String, Any?> {
    return this.fields.associate {
        it.name to it.value.decodeToAny()
    }
}

fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Array<*> -> JsonArray(map { it.toJsonElement() })
    is List<*> -> JsonArray(map { it.toJsonElement() })
    is Map<*, *> -> JsonObject(map { it.key.toString() to it.value.toJsonElement() }.toMap())
    else -> Json.encodeToJsonElement(serializer(this::class.createType()), this)
}

fun <T : Any> toMap(obj: T): Map<String, Any?> {
    return (obj::class as KClass<T>).memberProperties.associate { prop ->
        prop.name to prop.get(obj)?.let { value ->
            if (value::class.isData) {
                toMap(value)
            } else {
                value
            }
        }
    }
}

open class VoidField : Field<Void>(TYPE_VOID, null)

open class OptionalField(value: Field<*>?) : Field<Field<*>>(TYPE_OPTIONAL, value)

open class BooleanField(value: Boolean) : Field<Boolean>(TYPE_BOOLEAN, value)

open class StringField(value: String) : Field<String>(TYPE_STRING, value)

open class NumberField(type: String, value: String) : Field<String>(type, value) {
    fun toUByte(): UByte? = value?.toInt()?.toUByte()
    fun toByte(): Byte? = value?.toInt()?.toByte()
    fun toUShort(): UShort? = value?.toUShort()
    fun toShort(): Short? = value?.toShort()
    fun toUInt(): UInt? = value?.toUInt()
    fun toInt(): Int? = value?.toInt()
    fun toULong(): ULong? = value?.toULong()
    fun toLong(): Long? = value?.toLong()
    fun toBigInteger(): BigInteger? = value?.toBigInteger()
    fun toFloat(): Float? = value?.toFloat()
    fun toDouble(): Double? = value?.toDouble()
    fun toBigDecimal(): BigDecimal? = value?.toBigDecimal()
}

open class IntNumberField(value: String) : NumberField(TYPE_INT, value)
open class UIntNumberField(value: String) : NumberField(TYPE_UINT, value)
open class Int8NumberField(value: String) : NumberField(TYPE_INT8, value)
open class UInt8NumberField(value: String) : NumberField(TYPE_UINT8, value)
open class Int16NumberField(value: String) : NumberField(TYPE_INT16, value)
open class UInt16NumberField(value: String) : NumberField(TYPE_UINT16, value)
open class Int32NumberField(value: String) : NumberField(TYPE_INT32, value)
open class UInt32NumberField(value: String) : NumberField(TYPE_UINT32, value)
open class Int64NumberField(value: String) : NumberField(TYPE_INT64, value)
open class UInt64NumberField(value: String) : NumberField(TYPE_UINT64, value)
open class Int128NumberField(value: String) : NumberField(TYPE_INT128, value)
open class UInt128NumberField(value: String) : NumberField(TYPE_UINT128, value)
open class Int256NumberField(value: String) : NumberField(TYPE_INT256, value)
open class UInt256NumberField(value: String) : NumberField(TYPE_UINT256, value)
open class Word8NumberField(value: String) : NumberField(TYPE_WORD8, value)
open class Word16NumberField(value: String) : NumberField(TYPE_WORD16, value)
open class Word32NumberField(value: String) : NumberField(TYPE_WORD32, value)
open class Word64NumberField(value: String) : NumberField(TYPE_WORD64, value)
open class Fix64NumberField(value: String) : NumberField(TYPE_FIX64, value)
open class UFix64NumberField(value: String) : NumberField(TYPE_UFIX64, value)

open class ArrayField(value: Array<Field<*>>) : Field<Array<Field<*>>>(TYPE_ARRAY, value) {
    constructor(value: Iterable<Field<*>>) : this(value.toList().toTypedArray())

    override fun hashCode(): Int {
        return value?.contentHashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return true
    }
}

open class DictionaryField(value: Array<DictionaryFieldEntry>) : Field<Array<DictionaryFieldEntry>>(TYPE_DICTIONARY, value) {
    constructor(value: Iterable<DictionaryFieldEntry>) : this(value.toList().toTypedArray())

    companion object {
        private fun fromPairs(value: Iterable<Pair<Field<*>, Field<*>>>): DictionaryField {
            return DictionaryField(value.map { DictionaryFieldEntry(it) }.toTypedArray())
        }

        fun <K, V> fromMap(value: Map<K, V>, keys: (K) -> Field<*>, values: (V) -> Field<*>): DictionaryField {
            return fromPairs(value.mapKeys { keys(it.key) }.mapValues { values(it.value) }.map { Pair(it.key, it.value) })
        }
    }
}

open class DictionaryFieldEntry(val key: Field<*>, val value: Field<*>) : Serializable {
    constructor(pair: Pair<Field<*>, Field<*>>) : this(pair.first, pair.second)
}

open class AddressField(value: String) : Field<String>(
    TYPE_ADDRESS,
    if (!value.lowercase().startsWith("0x")) {
        "0x$value"
    } else {
        value
    }
) {
    constructor(bytes: ByteArray) : this(bytes.bytesToHex())
}

@kotlinx.serialization.Serializable
open class PathValue(val domain: String, val identifier: String) : Serializable {
    override fun hashCode(): Int {
        return Objects.hash(domain, identifier)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PathValue

        return domain == other.domain && identifier == other.identifier
    }
}

open class PathField(value: PathValue) : Field<PathValue>(TYPE_PATH, value) {
    override fun hashCode(): Int {
        return Objects.hash(type, value?.domain, value?.identifier)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as PathField

        return value?.domain == other.value?.domain && value?.identifier == other.value?.identifier
    }
}

@kotlinx.serialization.Serializable
open class CapabilityValue(val path: String, val address: String, val borrowType: String) : Serializable
open class CapabilityField(value: CapabilityValue) : Field<CapabilityValue>(TYPE_CAPABILITY, value)

open class CompositeField(type: String, value: CompositeValue) : Field<CompositeValue>(type, value) {
    val id: String? @JsonIgnore get() = value?.id
    operator fun <T : Field<*>> get(name: String): T? = value?.getField(name)
    operator fun contains(name: String): Boolean = value?.getField<Field<*>>(name) != null
}

open class CompositeAttribute(val name: String, val value: Field<*>) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompositeAttribute) return false

        if (name != other.name) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}

open class CompositeValue(val id: String, val fields: Array<CompositeAttribute>) : Serializable {
    @Suppress("UNCHECKED_CAST")
    fun <T : Field<*>> getField(name: String): T? = fields.find { it.name == name }?.value as T?
    fun <T : Field<*>> getRequiredField(name: String): T = getField(name) ?: throw IllegalStateException("Value for $name not found")
    inline operator fun <reified T : Field<*>> get(name: String): T? {
        val field = fields.find { it.name == name }?.value
        return if (field is T) {
            field
        } else {
            null
        }
    }
    operator fun contains(name: String): Boolean = fields.find { it.name == name } != null
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + fields.contentHashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompositeValue) return false

        if (id != other.id) return false
        if (!fields.contentEquals(other.fields)) return false

        return true
    }
}

open class StructField(value: CompositeValue) : CompositeField(TYPE_STRUCT, value)
open class ResourceField(value: CompositeValue) : CompositeField(TYPE_RESOURCE, value)
open class EventField(value: CompositeValue) : CompositeField(TYPE_EVENT, value)
open class ContractField(value: CompositeValue) : CompositeField(TYPE_CONTRACT, value)
open class EnumField(value: CompositeValue) : CompositeField(TYPE_ENUM, value)
open class TypeValue(val staticType: CadenceType) : Serializable
open class TypeField(value: TypeValue) : Field<TypeValue>(TYPE_TYPE, value)

open class InitializerType(
    val label: String,
    val id: String,
    val type: CadenceType
) : Serializable

open class FieldType(
    val id: String,
    val type: CadenceType
) : Serializable

open class ParameterType(
    val label: String,
    val id: String,
    val type: CadenceType
) : Serializable

// TODO: this JsonDeserializer.None::class is lame, but
// it's the only way I could figure out how to deserialize
// a polymorphic object using  multiple of it's fields.
// this is necessary for the partial types (for repeated types)
// https://docs.onflow.org/cadence/json-cadence-spec/#repeated-types

@JsonDeserialize(using = CadenceTypeDeserializer::class)
abstract class CadenceType(val kind: String) : Serializable

@JsonDeserialize(using = JsonDeserializer.None::class)
open class PartialCadenceType(kind: String, val type: String) : CadenceType(kind)

@JsonDeserialize(using = JsonDeserializer.None::class)
open class SimpleType(kind: String) : CadenceType(kind)

@JsonDeserialize(using = JsonDeserializer.None::class)
open class OptionalType(val type: CadenceType) : CadenceType(TYPE_OPTIONAL)

@JsonDeserialize(using = JsonDeserializer.None::class)
open class VariableSizedArrayType(val type: CadenceType) : CadenceType(TYPE_VARIABLE_SIZED_ARRAY)

@JsonDeserialize(using = JsonDeserializer.None::class)
open class ConstantSizedArrayType(val type: CadenceType) : CadenceType(TYPE_CONSTANT_SIZED_ARRAY)

@JsonDeserialize(using = JsonDeserializer.None::class)
open class DictionaryType(val key: CadenceType, val value: CadenceType) : CadenceType(TYPE_DICTIONARY)

@JsonDeserialize(using = JsonDeserializer.None::class)
open class CompositeType(
    kind: String,
    val type: String,
    val typeID: String,
    val initializers: Array<InitializerType>,
    val fields: Array<FieldType>
) : CadenceType(kind)

@JsonDeserialize(using = JsonDeserializer.None::class)
open class FunctionType(
    val typeID: String,
    val parameters: Array<ParameterType>,
    val `return`: CadenceType
) : CadenceType(TYPE_FUNCTION)

@JsonDeserialize(using = JsonDeserializer.None::class)
open class ReferenceType(
    val typeID: String?,
    val authorized: Boolean,
    val type: CadenceType
) : CadenceType(TYPE_REFERENCE)

@JsonDeserialize(using = JsonDeserializer.None::class)
open class RestrictionType(
    val typeID: String,
    val type: CadenceType,
    val restrictions: Array<CadenceType>
) : CadenceType(TYPE_RESTRICTION)

@JsonDeserialize(using = JsonDeserializer.None::class)
open class CapabilityType(
    val type: CadenceType
) : CadenceType(TYPE_CAPABILITY)

@JsonDeserialize(using = JsonDeserializer.None::class)
open class EnumType(
    val type: CadenceType,
    val typeID: String,
    val initializers: Array<InitializerType>,
    val fields: Array<FieldType>
) : CadenceType(TYPE_ENUM)

class CadenceTypeDeserializer(vc: Class<*>?) : StdDeserializer<CadenceType>(vc) {
    constructor() : this(null)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): CadenceType {
        val node = p.codec.readTree<JsonNode>(p)
        if (!node.isObject) {
            throw MismatchedInputException.from(p, "Expected object for CadenceType, found ${node.nodeType} instead")
        }

        val kind = node.get("kind")?.asText()
            ?: throw MismatchedInputException.from(p, "kind not specified for CadenceType")
        val type = node.get("type")

        if (type != null && type.isTextual && !type.textValue().isNullOrBlank()) {
            return PartialCadenceType(
                kind = kind,
                type = type.asText()
            )
        }

        return when (kind) {
            TYPE_ANY, TYPE_ANYSTRUCT, TYPE_ANYRESOURCE, TYPE_TYPE, TYPE_VOID, TYPE_NEVER, TYPE_BOOLEAN,
            TYPE_STRING, TYPE_CHARACTER, TYPE_BYTES, TYPE_NUMBER, TYPE_SIGNEDNUMBER, TYPE_INTEGER, TYPE_SIGNEDINTEGER,
            TYPE_FIXEDPOINT, TYPE_SIGNEDFIXEDPOINT, TYPE_INT, TYPE_UINT, TYPE_INT8, TYPE_UINT8, TYPE_INT16,
            TYPE_UINT16, TYPE_INT32, TYPE_UINT32, TYPE_INT64, TYPE_UINT64, TYPE_INT128, TYPE_UINT128, TYPE_INT256,
            TYPE_UINT256, TYPE_WORD8, TYPE_WORD16, TYPE_WORD32, TYPE_WORD64, TYPE_FIX64, TYPE_UFIX64, TYPE_ARRAY,
            TYPE_ADDRESS, TYPE_PATH, TYPE_CAPABILITYPATH, TYPE_STORAGEPATH, TYPE_PUBLICPATH,
            TYPE_PRIVATEPATH, TYPE_AUTHACCOUNT, TYPE_PUBLICACCOUNT, TYPE_AUTHACCOUNT_KEYS, TYPE_PUBLICACCOUNT_KEYS,
            TYPE_AUTHACCOUNT_CONTRACTS, TYPE_PUBLICACCOUNT_CONTRACTS, TYPE_DEPLOYEDCONTRACT, TYPE_ACCOUNTKEY,
            TYPE_BLOCK -> {
                p.codec.treeToValue(node, SimpleType::class.java)
            }

            TYPE_STRUCT, TYPE_RESOURCE, TYPE_EVENT, TYPE_CONTRACT, TYPE_STRUCT_INTERFACE, TYPE_RESOURCE_INTERFACE,
            TYPE_CONTRACT_INTERFACE -> {
                p.codec.treeToValue(node, CompositeType::class.java)
            }

            TYPE_OPTIONAL -> {
                p.codec.treeToValue(node, OptionalType::class.java)
            }

            TYPE_VARIABLE_SIZED_ARRAY -> {
                p.codec.treeToValue(node, VariableSizedArrayType::class.java)
            }

            TYPE_CONSTANT_SIZED_ARRAY -> {
                p.codec.treeToValue(node, ConstantSizedArrayType::class.java)
            }

            TYPE_DICTIONARY -> {
                p.codec.treeToValue(node, DictionaryType::class.java)
            }

            TYPE_FUNCTION -> {
                p.codec.treeToValue(node, FunctionType::class.java)
            }

            TYPE_REFERENCE -> {
                p.codec.treeToValue(node, ReferenceType::class.java)
            }

            TYPE_RESTRICTION -> {
                p.codec.treeToValue(node, RestrictionType::class.java)
            }

            TYPE_CAPABILITY -> {
                p.codec.treeToValue(node, CapabilityType::class.java)
            }

            TYPE_ENUM -> {
                p.codec.treeToValue(node, EnumType::class.java)
            }

            else -> throw MismatchedInputException.from(p, "Unknown CadenceType kind: $kind")
        }
    }
}
