package org.onflow.examples.kotlin.getBlock

import org.onflow.flow.sdk.*

internal class GetBlockAccessAPIConnector(
    private val accessAPI: FlowAccessApi
) {
    fun getLatestSealedBlock(): FlowBlock {
        val isSealed = true
        return when (val response = accessAPI.getLatestBlock(isSealed)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
    }

    fun getBlockByID(blockID: FlowId): FlowBlock =
        when (val response = accessAPI.getBlockById(blockID)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

    fun getBlockByHeight(height: Long): FlowBlock =
        when (val response = accessAPI.getBlockByHeight(height)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
}
