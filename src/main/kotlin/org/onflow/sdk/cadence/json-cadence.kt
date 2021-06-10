package org.onflow.sdk.cadence

// This files contains types for the JSON-Cadence Data Interchange Format

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.onflow.sdk.bytesToHex
import java.math.BigDecimal
import java.math.BigInteger

const val TYPE_VOID = "Void"
const val TYPE_OPTIONAL = "Optional"
const val TYPE_BOOLEAN = "Bool"
const val TYPE_STRING = "String"
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
const val TYPE_DICTIONARY = "Dictionary"
const val TYPE_ADDRESS = "Address"
const val TYPE_PATH = "Path"
const val TYPE_CAPABILITY = "Capability"
const val TYPE_STRUCT = "Struct"
const val TYPE_RESOURCE = "Resource"
const val TYPE_EVENT = "Event"
const val TYPE_CONTRACT = "Contract"
const val TYPE_ENUM = "Enum"

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
        Type(value = EnumField::class, name = TYPE_ENUM)
    ]
)
abstract class Field<T> constructor(
    val type: String,
    val value: T?
)

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
}

open class DictionaryField(value: Array<DictionaryFieldEntry>) : Field<Array<DictionaryFieldEntry>>(TYPE_DICTIONARY, value) {
    constructor(value: Iterable<DictionaryFieldEntry>) : this(value.toList().toTypedArray())
    companion object {
        fun fromPairs(value: Iterable<Pair<Field<*>, Field<*>>>): DictionaryField {
            return DictionaryField(value.map { DictionaryFieldEntry(it) }.toTypedArray())
        }
        fun <K, V> fromMap(value: Map<K, V>, keys: (K) -> Field<*>, values: (V) -> Field<*>): DictionaryField {
            return fromPairs(value.mapKeys { keys(it.key) }.mapValues { values(it.value) }.map { Pair(it.key, it.value) })
        }
    }
}
open class DictionaryFieldEntry(val key: Field<*>, val value: Field<*>) {
    constructor(pair: Pair<Field<*>, Field<*>>) : this(pair.first, pair.second)
}

open class AddressField(value: String) : Field<String>(TYPE_ADDRESS, if (!value.lowercase().startsWith("0x")) { "0x$value" } else { value }) {
    constructor(bytes: ByteArray) : this(bytes.bytesToHex())
}

open class PathValue(val domain: String, val identifier: String)
open class PathField(value: PathValue) : Field<PathValue>(TYPE_PATH, value)

open class CapabilityValue(val path: String, val address: String, val borrowType: String)
open class CapabilityField(value: CapabilityValue) : Field<CapabilityValue>(TYPE_CAPABILITY, value)

open class CompositeField(type: String, value: CompositeValue) : Field<CompositeValue>(type, value) {
    val id: String? @JsonIgnore get() = value?.id
    operator fun <T : Field<*>> get(name: String): T? = value?.getField(name)
    operator fun contains(name: String): Boolean = value?.getField<Field<*>>(name) != null
}
open class CompositeAttribute(val name: String, val value: Field<*>)
open class CompositeValue(val id: String, val fields: Array<CompositeAttribute>) {
    @Suppress("UNCHECKED_CAST")
    fun <T : Field<*>> getField(name: String): T? = fields.find { it.name == name }?.value as T?
    fun <T : Field<*>> getRequiredField(name: String): T = getField(name) ?: throw IllegalStateException("Value for $name not found")
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(name: String): T? = getField<Field<*>>(name)?.value as T?
    operator fun contains(name: String): Boolean = fields.find { it.name == name } != null
}
open class StructField(value: CompositeValue) : CompositeField(TYPE_STRUCT, value)
open class ResourceField(value: CompositeValue) : CompositeField(TYPE_RESOURCE, value)
open class EventField(value: CompositeValue) : CompositeField(TYPE_EVENT, value)
open class ContractField(value: CompositeValue) : CompositeField(TYPE_CONTRACT, value)
open class EnumField(value: CompositeValue) : CompositeField(TYPE_ENUM, value)
