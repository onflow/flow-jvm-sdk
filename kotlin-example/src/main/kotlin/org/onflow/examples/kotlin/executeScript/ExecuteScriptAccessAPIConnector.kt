package org.onflow.examples.kotlin.executeScript

import com.google.protobuf.ByteString
import org.onflow.examples.kotlin.ExamplesUtils
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.JsonCadenceBuilder
import java.math.BigDecimal

internal class ExecuteScriptAccessAPIConnector(
    private val accessAPI: FlowAccessApi
) {
    fun executeSimpleScript(): FlowScriptResponse {
        val loadedScript = ExamplesUtils.loadScriptContent("cadence/execute_simple_script_example.cdc")

        return accessAPI.simpleFlowScript {
            script { loadedScript }
            arg { JsonCadenceBuilder().int(5) }
        }.let { response ->
            when (response) {
                is FlowAccessApi.AccessApiCallResponse.Success -> response.data
                is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
            }
        }
    }

    fun executeComplexScript(): User {
        val loadedScript = ExamplesUtils.loadScriptContent("cadence/execute_complex_script_example.cdc")

        val value = accessAPI.simpleFlowScript {
            script { loadedScript }
            arg { JsonCadenceBuilder().string("my_name") }
        }.let { response ->
            when (response) {
                is FlowAccessApi.AccessApiCallResponse.Success -> response.data
                is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
            }
        }

        val struct = value.jsonCadence.decode<User>()
        return struct
    }

    data class User(
        val balance: BigDecimal,
        val address: FlowAddress,
        val name: String
    )
}
