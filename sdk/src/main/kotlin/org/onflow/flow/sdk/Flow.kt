package org.onflow.flow.sdk

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.onflow.protobuf.access.AccessAPIGrpc
import org.onflow.flow.sdk.cadence.CadenceNamespace
import org.onflow.flow.sdk.cadence.Field
import org.onflow.flow.sdk.cadence.JsonCadenceMarshalling
import org.onflow.flow.sdk.impl.AsyncFlowAccessApiImpl
import org.onflow.flow.sdk.impl.FlowAccessApiImpl
import org.onflow.protobuf.executiondata.ExecutionDataAPIGrpc
import kotlin.reflect.KClass

object Flow {
    private const val DEFAULT_USER_AGENT = "Flow JVM SDK"
    private const val DEFAULT_MAX_MESSAGE_SIZE = 16777216

    private var OBJECT_MAPPER: ObjectMapper = ObjectMapper()

    var DEFAULT_CHAIN_ID: FlowChainId = FlowChainId.MAINNET
        private set

    var DEFAULT_ADDRESS_REGISTRY: AddressRegistry = AddressRegistry()
        private set

    init {
        OBJECT_MAPPER.registerKotlinModule()
        OBJECT_MAPPER.findAndRegisterModules()
    }

    @JvmStatic
    @JvmOverloads
    fun configureDefaults(
        chainId: FlowChainId = DEFAULT_CHAIN_ID,
        addressRegistry: AddressRegistry = DEFAULT_ADDRESS_REGISTRY
    ) {
        DEFAULT_CHAIN_ID = chainId
        DEFAULT_ADDRESS_REGISTRY = addressRegistry
    }

    @JvmStatic
    @JvmOverloads
    fun newAccessApi(host: String, port: Int = 9000, secure: Boolean = false, userAgent: String = DEFAULT_USER_AGENT, maxMessageSize: Int = DEFAULT_MAX_MESSAGE_SIZE): FlowAccessApi {
        val channel = openChannel(host, port, secure, userAgent, maxMessageSize)
        return FlowAccessApiImpl(AccessAPIGrpc.newBlockingStub(channel), ExecutionDataAPIGrpc.newBlockingStub(channel))
    }

    @JvmStatic
    @JvmOverloads
    fun newAsyncAccessApi(host: String, port: Int = 9000, secure: Boolean = false, userAgent: String = DEFAULT_USER_AGENT, maxMessageSize: Int = DEFAULT_MAX_MESSAGE_SIZE): AsyncFlowAccessApi {
        val channel = openChannel(host, port, secure, userAgent, maxMessageSize)
        return AsyncFlowAccessApiImpl(AccessAPIGrpc.newFutureStub(channel))
    }

    @JvmStatic
    private fun openChannel(host: String, port: Int, secure: Boolean, userAgent: String, maxMessageSize: Int): ManagedChannel {
        var channelBuilder = ManagedChannelBuilder
            .forAddress(host, port)
            .userAgent(userAgent)
            .maxInboundMessageSize(maxMessageSize)

        channelBuilder = if (secure) {
            channelBuilder.useTransportSecurity()
        } else {
            channelBuilder.usePlaintext()
        }

        return channelBuilder.build()
    }

    @JvmStatic
    fun <T : Field<*>> decodeJsonCadenceList(string: String): List<T> = decodeJsonCadenceList(string.toByteArray(Charsets.UTF_8))

    @JvmStatic
    fun <T : Field<*>> decodeJsonCadenceList(bytes: ByteArray): List<T> = OBJECT_MAPPER.readValue(bytes, object : TypeReference<List<T>>() {})

    @JvmStatic
    fun <T : Field<*>> decodeJsonCadence(string: String): T = decodeJsonCadence(string.toByteArray(Charsets.UTF_8))

    @JvmStatic
    fun <T : Field<*>> decodeJsonCadence(bytes: ByteArray): T = OBJECT_MAPPER.readValue(bytes, object : TypeReference<T>() {})

    @JvmStatic
    fun <T : Field<*>> encodeJsonCadenceList(jsonCadences: Iterable<T>): ByteArray = OBJECT_MAPPER.writeValueAsBytes(jsonCadences)

    @JvmStatic
    fun <T : Field<*>> encodeJsonCadence(jsonCadence: T): ByteArray = OBJECT_MAPPER.writeValueAsBytes(jsonCadence)

    @JvmStatic
    fun <T : Any> unmarshall(type: KClass<T>, value: Field<*>, namespace: FlowAddress): T = JsonCadenceMarshalling.unmarshall(type, value, namespace)

    @JvmStatic
    @JvmOverloads
    fun <T : Any> unmarshall(type: KClass<T>, value: Field<*>, namespace: CadenceNamespace = CadenceNamespace()): T = JsonCadenceMarshalling.unmarshall(type, value, namespace)

    @JvmStatic
    fun <T : Any> marshall(value: T, clazz: KClass<out T>, namespace: FlowAddress): Field<*> = JsonCadenceMarshalling.marshall(value, clazz, namespace)

    @JvmStatic
    @JvmOverloads
    fun <T : Any> marshall(value: T, clazz: KClass<out T>, namespace: CadenceNamespace = CadenceNamespace()): Field<*> = JsonCadenceMarshalling.marshall(value, clazz, namespace)

    @JvmStatic
    fun <T : Any> marshall(value: T, namespace: FlowAddress): Field<*> = JsonCadenceMarshalling.marshall(value, namespace)

    @JvmStatic
    @JvmOverloads
    fun <T : Any> marshall(value: T, namespace: CadenceNamespace = CadenceNamespace()): Field<*> = JsonCadenceMarshalling.marshall(value, namespace)
}
