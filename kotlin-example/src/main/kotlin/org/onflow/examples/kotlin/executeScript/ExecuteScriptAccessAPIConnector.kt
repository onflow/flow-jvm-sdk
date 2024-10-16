package org.onflow.examples.kotlin.executeScript

import kotlinx.serialization.Serializable
import org.onflow.examples.kotlin.ExamplesUtils
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.cadence.*

class ExecuteScriptAccessAPIConnector(
    private val accessAPI: FlowAccessApi
) {
    fun executeSimpleScript(): FlowScriptResponse {
        val loadedScript = ExamplesUtils.loadScriptContent("cadence/execute_simple_script_example.cdc")

        return accessAPI
            .simpleFlowScript {
                script { loadedScript }
                arg { JsonCadenceBuilder().int(5) }
            }.let { response ->
                when (response) {
                    is FlowAccessApi.AccessApiCallResponse.Success -> response.data
                    is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
                }
            }
    }

    fun executeComplexScript(): FlowScriptResponse {
        val loadedScript = ExamplesUtils.loadScriptContent("cadence/execute_complex_script_example.cdc")

        return accessAPI
            .simpleFlowScript {
                script { loadedScript }
                arg { JsonCadenceBuilder().address("0x84221fe0294044d7") }
            }.let { response ->
                when (response) {
                    is FlowAccessApi.AccessApiCallResponse.Success -> response.data
                    is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
                }
            }
    }

    @Serializable
    data class StorageInfo(
        val capacity: Int,
        val used: Int,
        val available: Int
    )
}
