package org.onflow.examples.kotlin.getExecutionData

import org.onflow.flow.sdk.*

internal class GetExecutionDataAccessAPIConnector(private val accessAPI: FlowAccessApi) {

    fun getExecutionDataByBlockId(blockId: FlowId): FlowExecutionResult {
        return when (val response = accessAPI.getExecutionResultByBlockId(blockId)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
    }
}

