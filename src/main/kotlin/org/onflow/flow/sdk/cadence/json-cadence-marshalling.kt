package org.onflow.flow.sdk.cadence

import org.onflow.flow.sdk.Flow
import org.onflow.flow.sdk.FlowAddress
import org.onflow.flow.sdk.cadence.CadenceNamespace.Companion.ns
import java.lang.annotation.Inherited
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

@Inherited
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonCadenceConversion(
    val converter: KClass<out JsonCadenceConverter<*>>
)

data class CadenceNamespace(
    val parts: List<String> = emptyList()
) {
    companion object {
        @JvmStatic
        fun ns(vararg values: String): CadenceNamespace = CadenceNamespace(*values)

        @JvmStatic
        fun ns(address: FlowAddress): CadenceNamespace = CadenceNamespace("A", address.base16Value)

        @JvmStatic
        fun ns(address: FlowAddress, vararg values: String): CadenceNamespace = CadenceNamespace("A", address.base16Value, *values)
    }

    constructor(vararg value: String) : this(value.toList())

    val value: String = parts.joinToString(separator = ".")

    fun withNamespace(id: String): String = (parts + id).joinToString(separator = ".")

    fun withoutNamespace(id: String): String = id.replace("$value.", "")

    fun push(namespace: String): CadenceNamespace = this.copy(
        parts = this.parts + namespace
    )

    fun push(address: FlowAddress): CadenceNamespace = push(address.base16Value)

    fun pop(count: Int = 1): CadenceNamespace = this.copy(
        parts = this.parts.dropLast(count)
    )

    operator fun plus(element: String): CadenceNamespace = this.push(element)
    operator fun plus(element: FlowAddress): CadenceNamespace = this.push(element)
}

interface JsonCadenceConverter<T> {
    fun unmarshall(value: Field<*>, namespace: CadenceNamespace): T {
        throw UnsupportedOperationException("${this::class.simpleName} cannot deserialize $value")
    }
    fun marshall(value: T, namespace: CadenceNamespace): Field<*> {
        throw UnsupportedOperationException("${this::class.simpleName} cannot serializer $value")
    }
}

object JsonCadenceMarshalling {
    private val MARSHALLER_CACHE_JSON: MutableMap<KClass<*>, JsonCadenceConverter<*>> = mutableMapOf()

    @JvmStatic
    @JvmOverloads
    @Synchronized
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getSerializer(type: KClass<out T>, cache: Boolean = true): JsonCadenceConverter<T> {
        var ret = if (cache) {
            MARSHALLER_CACHE_JSON[type]
        } else {
            null
        }
        if (ret == null) {
            ret = type.annotations
                .find { it is JsonCadenceConversion }
                ?.let { it as JsonCadenceConversion }
                ?.converter
                ?.createInstance()
        }
        if (ret != null && cache) {
            MARSHALLER_CACHE_JSON[type] = ret
        } else if (ret == null) {
            throw IllegalArgumentException("No JsonCadenceSerializer found for ${type.simpleName}")
        }
        return ret as JsonCadenceConverter<T>
    }

    @JvmStatic
    fun <T : Any> unmarshall(type: KClass<T>, value: Field<*>, namespace: FlowAddress): T = getSerializer(type).unmarshall(value, ns(namespace))

    @JvmStatic
    @JvmOverloads
    fun <T : Any> unmarshall(type: KClass<T>, value: Field<*>, namespace: CadenceNamespace = CadenceNamespace()): T = getSerializer(type).unmarshall(value, namespace)

    @JvmStatic
    fun <T : Any> marshall(value: T, clazz: KClass<out T>, namespace: FlowAddress): Field<*> = getSerializer(clazz).marshall(value, ns(namespace))

    @JvmStatic
    @JvmOverloads
    fun <T : Any> marshall(value: T, clazz: KClass<out T>, namespace: CadenceNamespace = CadenceNamespace()): Field<*> = getSerializer(clazz).marshall(value, namespace)

    @JvmStatic
    fun <T : Any> marshall(value: T, namespace: FlowAddress): Field<*> = getSerializer(value::class).marshall(value, ns(namespace))

    @JvmStatic
    @JvmOverloads
    fun <T : Any> marshall(value: T, namespace: CadenceNamespace = CadenceNamespace()): Field<*> = getSerializer(value::class).marshall(value, namespace)
}

fun <T : Field<*>> marshall(block: JsonCadenceBuilder.() -> T): T = block(JsonCadenceBuilder())

class JsonCadenceBuilder {
    fun <T : Any> marshall(value: T, clazz: KClass<out T> = value::class, namespace: FlowAddress): Field<*> = Flow.marshall(value, clazz, namespace)
    fun <T : Any> marshall(value: T, clazz: KClass<out T> = value::class, namespace: CadenceNamespace = CadenceNamespace()): Field<*> = Flow.marshall(value, clazz, namespace)
    fun void(): VoidField = VoidField()
    fun optional(block: JsonCadenceBuilder.() -> Field<*>?): OptionalField = OptionalField(block())
    fun optional(value: Field<*>?): OptionalField = OptionalField(value)
    fun <T : Any> optional(value: T?, block: JsonCadenceBuilder.(T) -> Field<*>?): OptionalField = OptionalField(value?.let { block(it) })
    fun boolean(value: Boolean): BooleanField = BooleanField(value)
    fun string(value: String): StringField = StringField(value)
    fun array(value: Iterable<Field<*>>): ArrayField = ArrayField(value)
    fun <T> array(value: Iterable<T>, mapper: JsonCadenceBuilder.(T) -> Field<*>): ArrayField = ArrayField(value.map { mapper(it) })
    fun array(block: JsonCadenceBuilder.() -> Iterable<Field<*>>): ArrayField = ArrayField(block().toList().toTypedArray())
    fun byteArray(value: Array<UByte>): ArrayField = ArrayField(value.map { uint8(it) })
    fun byteArray(value: ByteArray): ArrayField = byteArray(value.map { it.toUByte() }.toTypedArray())
    fun dictionary(value: Iterable<DictionaryFieldEntry>): DictionaryField = DictionaryField(value)
    fun dictionary(block: JsonCadenceBuilder.() -> Array<DictionaryFieldEntry>): DictionaryField = DictionaryField(block().toList().toTypedArray())
    fun dictionaryOfMap(block: JsonCadenceBuilder.() -> Map<Field<*>, Field<*>>): DictionaryField = DictionaryField(block().map { DictionaryFieldEntry(it.key, it.value) })
    fun dictionaryOfNamedMap(block: JsonCadenceBuilder.() -> Map<String, Field<*>>): DictionaryField = DictionaryField(block().map { DictionaryFieldEntry(StringField(it.key), it.value) })
    fun dictionaryOfPairs(block: JsonCadenceBuilder.() -> Iterable<Pair<Field<*>, Field<*>>>): DictionaryField = DictionaryField(block().map { DictionaryFieldEntry(it.first, it.second) })
    fun dictionaryOfNamedPairs(block: JsonCadenceBuilder.() -> Iterable<Pair<String, Field<*>>>): DictionaryField = DictionaryField(block().map { DictionaryFieldEntry(StringField(it.first), it.second) })
    fun <K, V> dictionary(value: Map<K, V>, mapper: JsonCadenceBuilder.(Map.Entry<K, V>) -> Pair<Field<*>, Field<*>>): DictionaryField = DictionaryField(value.map { DictionaryFieldEntry(mapper(it)) })
    fun <V> dictionaryOfNamed(value: Map<String, V>, mapper: JsonCadenceBuilder.(Map.Entry<String, V>) -> Pair<String, Field<*>>): DictionaryField = DictionaryField(value.map { DictionaryFieldEntry(mapper(it).let { p -> string(p.first) to p.second }) })
    fun address(value: FlowAddress): AddressField = AddressField(value.formatted)
    fun address(value: String): AddressField = AddressField(value)
    fun address(value: ByteArray): AddressField = AddressField(value)
    fun path(value: PathValue): PathField = PathField(value)
    fun path(domain: String, identifier: String): PathField = PathField(PathValue(domain, identifier))
    fun capability(value: CapabilityValue): CapabilityField = CapabilityField(value)
    fun capability(path: String, address: String, borrowType: String): CapabilityField = CapabilityField(CapabilityValue(path, address, borrowType))
    fun composite(id: String, fields: Array<CompositeAttribute>): CompositeValue = CompositeValue(id, fields.toList().toTypedArray())
    fun composite(id: String, fields: Iterable<Pair<String, Field<*>>>): CompositeValue = CompositeValue(id, fields.map { CompositeAttribute(it.first, it.second) }.toTypedArray())
    fun composite(id: String, fields: Map<String, Field<*>>): CompositeValue = CompositeValue(id, fields.map { CompositeAttribute(it.key, it.value) }.toTypedArray())
    fun composite(id: String, block: JsonCadenceBuilder.() -> Array<CompositeAttribute>): CompositeValue = CompositeValue(id, block().toList().toTypedArray())
    fun compositeOfPairs(id: String, block: JsonCadenceBuilder.() -> Iterable<Pair<String, Field<*>>>): CompositeValue = CompositeValue(id, block().map { CompositeAttribute(it.first, it.second) }.toTypedArray())
    fun compositeOfNamedMap(id: String, block: JsonCadenceBuilder.() -> Map<String, Field<*>>): CompositeValue = CompositeValue(id, block().map { CompositeAttribute(it.key, it.value) }.toTypedArray())
    fun struct(value: CompositeValue): StructField = StructField(value)
    fun struct(block: JsonCadenceBuilder.() -> CompositeValue): StructField = StructField(block())
    fun resource(value: CompositeValue): ResourceField = ResourceField(value)
    fun resource(block: JsonCadenceBuilder.() -> CompositeValue): ResourceField = ResourceField(block())
    fun event(value: CompositeValue): EventField = EventField(value)
    fun event(block: JsonCadenceBuilder.() -> CompositeValue): EventField = EventField(block())
    fun contract(value: CompositeValue): ContractField = ContractField(value)
    fun contract(block: JsonCadenceBuilder.() -> CompositeValue): ContractField = ContractField(block())
    fun <T : Enum<T>> enum(value: T, namespace: CadenceNamespace = CadenceNamespace()): EnumField = enum(namespace.withNamespace(value.declaringJavaClass.simpleName), uint8(value.ordinal))
    fun enum(value: CompositeValue): EnumField = EnumField(value)
    fun enum(block: JsonCadenceBuilder.() -> CompositeValue): EnumField = EnumField(block())
    fun enum(id: String, value: Field<*>): EnumField = EnumField(compositeOfPairs(id) { listOf("rawValue" to value) })
    fun number(type: String, value: String): NumberField = NumberField(type, value)
    fun number(type: String, value: Number): NumberField = NumberField(type, value.toString())
    fun int(value: Number): IntNumberField = IntNumberField(value.toString())
    fun uint(value: Number): UIntNumberField = UIntNumberField(value.toString())
    fun int8(value: Number): Int8NumberField = Int8NumberField(value.toString())
    fun uint8(value: Number): UInt8NumberField = UInt8NumberField(value.toString())
    fun uint8(value: UByte): UInt8NumberField = UInt8NumberField(value.toString())
    fun int16(value: Number): Int16NumberField = Int16NumberField(value.toString())
    fun uint16(value: Number): UInt16NumberField = UInt16NumberField(value.toString())
    fun uint16(value: UShort): UInt16NumberField = UInt16NumberField(value.toString())
    fun int32(value: Number): Int32NumberField = Int32NumberField(value.toString())
    fun uint32(value: Number): UInt32NumberField = UInt32NumberField(value.toString())
    fun uint32(value: UInt): UInt64NumberField = UInt64NumberField(value.toString())
    fun int64(value: Number): Int64NumberField = Int64NumberField(value.toString())
    fun uint64(value: Number): UInt64NumberField = UInt64NumberField(value.toString())
    fun uint64(value: ULong): UInt64NumberField = UInt64NumberField(value.toString())
    fun int128(value: Number): Int128NumberField = Int128NumberField(value.toString())
    fun uint128(value: Number): UInt128NumberField = UInt128NumberField(value.toString())
    fun int256(value: Number): Int256NumberField = Int256NumberField(value.toString())
    fun uint256(value: Number): UInt256NumberField = UInt256NumberField(value.toString())
    fun word8(value: Number): Word8NumberField = Word8NumberField(value.toString())
    fun word16(value: Number): Word16NumberField = Word16NumberField(value.toString())
    fun word32(value: Number): Word32NumberField = Word32NumberField(value.toString())
    fun word64(value: Number): Word64NumberField = Word64NumberField(value.toString())
    fun fix64(value: Number): Fix64NumberField = Fix64NumberField("%.8f".format(BigDecimal(value.toString())))
    fun ufix64(value: Number): UFix64NumberField = UFix64NumberField("%.8f".format(BigDecimal(value.toString())))
    fun fix64(value: String): Fix64NumberField = fix64(BigDecimal(value))
    fun ufix64(value: String): UFix64NumberField = ufix64(BigDecimal(value))
    fun int(value: String): IntNumberField = IntNumberField(value)
    fun uint(value: String): UIntNumberField = UIntNumberField(value)
    fun int8(value: String): Int8NumberField = Int8NumberField(value)
    fun uint8(value: String): UInt8NumberField = UInt8NumberField(value)
    fun int16(value: String): Int16NumberField = Int16NumberField(value)
    fun uint16(value: String): UInt16NumberField = UInt16NumberField(value)
    fun int32(value: String): Int32NumberField = Int32NumberField(value)
    fun uint32(value: String): UInt32NumberField = UInt32NumberField(value)
    fun int64(value: String): Int64NumberField = Int64NumberField(value)
    fun uint64(value: String): UInt64NumberField = UInt64NumberField(value)
    fun int128(value: String): Int128NumberField = Int128NumberField(value)
    fun uint128(value: String): UInt128NumberField = UInt128NumberField(value)
    fun int256(value: String): Int256NumberField = Int256NumberField(value)
    fun uint256(value: String): UInt256NumberField = UInt256NumberField(value)
    fun word8(value: String): Word8NumberField = Word8NumberField(value)
    fun word16(value: String): Word16NumberField = Word16NumberField(value)
    fun word32(value: String): Word32NumberField = Word32NumberField(value)
    fun word64(value: String): Word64NumberField = Word64NumberField(value)
}

fun <T> unmarshall(root: Field<*>, block: JsonCadenceParser.() -> T): T {
    val parser = JsonCadenceParser()
    if (root is CompositeField) {
        parser.push(root)
    }
    return block(parser)
}

class JsonCadenceParser {
    var compositeStack: MutableList<CompositeField> = mutableListOf()
    val composite: CompositeField get() = compositeStack.last()
    val compositeValue: CompositeValue get() = composite.value!!

    fun push(composite: CompositeField) = compositeStack.add(composite)
    fun pop(): CompositeField = compositeStack.removeLast()

    fun <T : Field<*>> field(name: String): T = compositeValue.getRequiredField(name)

    fun <T> withField(composite: CompositeField, block: JsonCadenceParser.() -> T): T {
        push(composite)
        try {
            return block(this)
        } finally {
            pop()
        }
    }

    inline fun <reified T : Any> unmarshall(name: String): T = Flow.unmarshall(T::class, field(name))
    inline fun <reified T : Any> unmarshall(name: String, namespace: CadenceNamespace = CadenceNamespace()): T = Flow.unmarshall(T::class, field(name), namespace)
    inline fun <reified T : Any> unmarshall(name: String, namespace: FlowAddress): T = Flow.unmarshall(T::class, field(name), namespace)

    inline fun <reified T : Any> unmarshall(name: String, type: KClass<T>): T = Flow.unmarshall(type, field(name))
    inline fun <reified T : Any> unmarshall(name: String, type: KClass<T>, namespace: CadenceNamespace = CadenceNamespace()): T = Flow.unmarshall(type, field(name), namespace)
    inline fun <reified T : Any> unmarshall(name: String, type: KClass<T>, namespace: FlowAddress): T = Flow.unmarshall(type, field(name), namespace)

    inline fun <reified T : Any> unmarshall(field: Field<*>, namespace: CadenceNamespace = CadenceNamespace()): T = Flow.unmarshall(T::class, field, namespace)
    inline fun <reified T : Any> unmarshall(field: Field<*>, namespace: FlowAddress): T = Flow.unmarshall(T::class, field, namespace)

    inline fun <reified T : Any> unmarshall(field: Field<*>, type: KClass<T>, namespace: CadenceNamespace = CadenceNamespace()): T = Flow.unmarshall(type, field, namespace)
    inline fun <reified T : Any> unmarshall(field: Field<*>, type: KClass<T>, namespace: FlowAddress): T = Flow.unmarshall(type, field, namespace)

    fun <T, F : Field<*>> field(name: String, block: JsonCadenceParser.(field: F) -> T): T = block(field(name))
    fun boolean(field: Field<*>): Boolean = (field as BooleanField).value!!
    fun string(field: Field<*>): String = (field as StringField).value!!
    fun address(field: Field<*>): String = (field as AddressField).value!!
    fun short(field: Field<*>): Short = (field as NumberField).toShort()!!
    fun ushort(field: Field<*>): UShort = (field as NumberField).toUShort()!!
    fun int(field: Field<*>): Int = (field as NumberField).toInt()!!
    fun uint(field: Field<*>): UInt = (field as NumberField).toUInt()!!
    fun long(field: Field<*>): Long = (field as NumberField).toLong()!!
    fun ulong(field: Field<*>): ULong = (field as NumberField).toULong()!!
    fun bigInteger(field: Field<*>): BigInteger = (field as NumberField).toBigInteger()!!
    fun float(field: Field<*>): Float = (field as NumberField).toFloat()!!
    fun double(field: Field<*>): Double = (field as NumberField).toDouble()!!
    fun bigDecimal(field: Field<*>): BigDecimal = (field as NumberField).toBigDecimal()!!
    fun <T> array(field: Field<*>, block: JsonCadenceParser.(field: ArrayField) -> T): T = block(field as ArrayField)
    fun <T> arrayValues(field: Field<*>, mapper: JsonCadenceParser.(field: Field<*>) -> T): List<T> = (field as ArrayField).value!!.map { mapper(it) }
    fun byteArray(field: Field<*>): ByteArray = arrayValues(field) { (it as UInt8NumberField).toByte()!! }.toByteArray()
    fun <T> dictionary(field: Field<*>, block: JsonCadenceParser.(field: DictionaryField) -> T): T = block(field as DictionaryField)
    fun <K, V> dictionaryPairs(field: Field<*>, mapper: JsonCadenceParser.(key: Field<*>, value: Field<*>) -> Pair<K, V>): List<Pair<K, V>> = (field as DictionaryField).value!!.map { mapper(it.key, it.value) }
    fun <K, V> dictionaryMap(field: Field<*>, mapper: JsonCadenceParser.(key: Field<*>, value: Field<*>) -> Pair<K, V>): Map<K, V> = dictionaryPairs(field, mapper).toMap()
    inline fun <reified T : Enum<T>, V : Field<*>> enum(field: Field<*>, crossinline mapper: (V) -> T): T {
        return withField(field as CompositeField) {
            val f = compositeValue.getRequiredField<V>("rawValue")
            mapper(f)
        }
    }
    inline fun <reified T : Enum<T>> enum(field: Field<*>): T = enum<T, UInt8NumberField>(field) { f -> f.toInt()!!.let { enumValues<T>()[it] } }
    fun <T> optional(field: Field<*>, block: JsonCadenceParser.(field: Field<*>) -> T): T? {
        if (field !is OptionalField) {
            throw IllegalArgumentException("field is not an OptionalField")
        }
        return field.value?.let { block(it) }
    }

    fun boolean(name: String): Boolean = boolean(field<BooleanField>(name))
    fun string(name: String): String = string(field<StringField>(name))
    fun address(name: String): String = address(field<AddressField>(name))
    fun short(name: String): Short = short(field<NumberField>(name))
    fun ushort(name: String): UShort = ushort(field<NumberField>(name))
    fun int(name: String): Int = int(field<NumberField>(name))
    fun uint(name: String): UInt = uint(field<NumberField>(name))
    fun long(name: String): Long = long(field<NumberField>(name))
    fun ulong(name: String): ULong = ulong(field<NumberField>(name))
    fun bigInteger(name: String): BigInteger = bigInteger(field<NumberField>(name))
    fun float(name: String): Float = float(field<NumberField>(name))
    fun double(name: String): Double = double(field<NumberField>(name))
    fun bigDecimal(name: String): BigDecimal = bigDecimal(field<NumberField>(name))
    fun <T> array(name: String, block: JsonCadenceParser.(field: ArrayField) -> T): T = array(field(name), block)
    fun <T> arrayValues(name: String, mapper: JsonCadenceParser.(field: Field<*>) -> T): List<T> = arrayValues(field<ArrayField>(name), mapper)
    fun byteArray(name: String): ByteArray = byteArray(field(name))
    fun <T> dictionary(name: String, block: JsonCadenceParser.(field: DictionaryField) -> T): T = dictionary(field(name), block)
    fun <K, V> dictionaryPairs(name: String, mapper: JsonCadenceParser.(key: Field<*>, value: Field<*>) -> Pair<K, V>): List<Pair<K, V>> = dictionaryPairs(field<DictionaryField>(name), mapper)
    fun <K, V> dictionaryMap(name: String, mapper: JsonCadenceParser.(key: Field<*>, value: Field<*>) -> Pair<K, V>): Map<K, V> = dictionaryPairs(name, mapper).toMap()
    inline fun <reified T : Enum<T>, V : Field<*>> enum(name: String, crossinline mapper: (V) -> T): T = enum(field(name), mapper)
    inline fun <reified T : Enum<T>> enum(name: String): T = enum(field(name))
    fun <T> optional(name: String, block: JsonCadenceParser.(field: Field<*>) -> T): T? = optional(field(name), block)
}
