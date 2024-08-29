package org.onflow.flow.sdk

import com.google.protobuf.UnsafeByteOperations
import org.onflow.flow.sdk.cadence.Field
import org.onflow.flow.sdk.cadence.JsonCadenceBuilder

fun flowScript(block: ScriptBuilder.() -> Unit): ScriptBuilder {
    val builder = ScriptBuilder()
    block(builder)
    return builder
}

private fun executeScript(api: FlowAccessApi, builder: ScriptBuilder): FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> {
    return try {
        val result = api.executeScriptAtLatestBlock(
            script = builder.script,
            arguments = builder.arguments.map { UnsafeByteOperations.unsafeWrap(Flow.encodeJsonCadence(it)) }
        )
        when (result) {
            is FlowAccessApi.AccessApiCallResponse.Success -> FlowAccessApi.AccessApiCallResponse.Success(result.data)
            is FlowAccessApi.AccessApiCallResponse.Error -> FlowAccessApi.AccessApiCallResponse.Error(result.message, result.throwable)
        }
    } catch (t: Throwable) {
        FlowAccessApi.AccessApiCallResponse.Error("Error while running script", t)
    }
}

fun FlowAccessApi.simpleFlowScript(block: ScriptBuilder.() -> Unit): FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> {
    val builder = flowScript(block)
    return executeScript(this, builder)
}

object FlowScriptHelper { // enables use of simpleFlowScript builder in Java
    @JvmStatic
    fun simpleFlowScript(api: FlowAccessApi, block: ScriptBuilder.() -> Unit): FlowAccessApi.AccessApiCallResponse<FlowScriptResponse> {
        val builder = flowScript(block)
        return executeScript(api, builder)
    }
}

class ScriptBuilder {
    private var addressRegistry: AddressRegistry = Flow.DEFAULT_ADDRESS_REGISTRY
    private var _chainId: FlowChainId = Flow.DEFAULT_CHAIN_ID
    private var _script: FlowScript? = null
    private var _arguments: MutableList<Field<*>> = mutableListOf()

    var script: FlowScript
        get() = _script!!
        set(value) { _script = value }

    fun script(script: FlowScript) {
        this.script = script
    }

    fun script(script: String, chainId: FlowChainId = _chainId, addresses: Map<String, FlowAddress> = mapOf()) = script(
        FlowScript(
            addressRegistry.processScript(
                script = script,
                chainId = chainId,
                addresses = addresses
            )
        )
    )

    fun script(code: ByteArray, chainId: FlowChainId = _chainId, addresses: Map<String, FlowAddress> = mapOf()) = script(String(code), chainId, addresses)

    fun script(chainId: FlowChainId = _chainId, addresses: Map<String, FlowAddress> = mapOf(), code: () -> String) = this.script(code(), chainId, addresses)

    var arguments: MutableList<Field<*>>
        get() = _arguments
        set(value) {
            _arguments.clear()
            _arguments.addAll(value)
        }

    fun arguments(arguments: MutableList<Field<*>>) {
        this.arguments = arguments
    }

    fun arguments(arguments: JsonCadenceBuilder.() -> Iterable<Field<*>>) {
        val builder = JsonCadenceBuilder()
        this.arguments = arguments(builder).toMutableList()
    }

    private fun arg(argument: Field<*>) = _arguments.add(argument)

    fun arg(argument: JsonCadenceBuilder.() -> Field<*>) = arg(argument(JsonCadenceBuilder()))
}
