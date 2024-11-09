package org.onflow.examples.kotlin.getTransaction

import org.onflow.flow.sdk.*

class GetTransactionAccessAPIConnector(
    private val accessAPI: FlowAccessApi
) {
    fun getTransaction(txID: FlowId): FlowTransaction =
        when (val response = accessAPI.getTransactionById(txID)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

    fun getSystemTransaction(blockId: FlowId): FlowTransaction =
        when (val response = accessAPI.getSystemTransaction(blockId)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

    fun getTransactionResult(txID: FlowId): FlowTransactionResult =
        when (val response = accessAPI.getTransactionResultById(txID)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

    fun getSystemTransactionResult(blockId: FlowId): FlowTransactionResult =
        when (val response = accessAPI.getSystemTransactionResult(blockId)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

    fun getTransactionResultByIndex(blockId: FlowId, index: Int): FlowTransactionResult =
        when (val response = accessAPI.getTransactionResultByIndex(blockId, index)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
}
