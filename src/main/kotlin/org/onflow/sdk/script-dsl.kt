package org.onflow.sdk

import com.google.protobuf.UnsafeByteOperations
import org.onflow.sdk.cadence.Field
import org.onflow.sdk.cadence.JsonCadenceBuilder

fun flowScript(block: ScriptBuilder.() -> Unit): ScriptBuilder {
    val ret = ScriptBuilder()
    block(ret)
    return ret
}

fun FlowAccessApi.simpleFlowScript(block: ScriptBuilder.() -> Unit): FlowScriptResponse {
    val api = this
    val builder = flowScript(block)
    return try {
        api.executeScriptAtLatestBlock(
            script = builder.script,
            arguments = builder.arguments.map { UnsafeByteOperations.unsafeWrap(Flow.encodeJsonCadence(it)) }
        )
    } catch (t: Throwable) {
        throw FlowException("Error while running script", t)
    }
}

class ScriptBuilder {

    var addressRegistry: AddressRegistry = Flow.DEFAULT_ADDRESS_REGISTRY
    private var _chainId: FlowChainId = Flow.DEFAULT_CHAIN_ID
    private var _script: FlowScript? = null
    private var _arguments: MutableList<Field<*>> = mutableListOf()

    var script: FlowScript
        get() { return _script!! }
        set(value) { _script = value }

    fun script(script: FlowScript) {
        this.script = script
    }
    fun script(code: String, chain: FlowChainId = _chainId) = script(
        FlowScript(
            addressRegistry.processScript(
                code,
                chain
            )
        )
    )
    fun script(code: ByteArray, chain: FlowChainId = _chainId) = script(String(code), chain)
    fun script(chain: FlowChainId = _chainId, code: () -> String) = this.script(code(), chain)

    var arguments: MutableList<Field<*>>
        get() { return _arguments }
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
    fun arg(argument: Field<*>) = _arguments.add(argument)
    fun arg(argument: JsonCadenceBuilder.() -> Field<*>) = arg(argument(JsonCadenceBuilder()))
}
