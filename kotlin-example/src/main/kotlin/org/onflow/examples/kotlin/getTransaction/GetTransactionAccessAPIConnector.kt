package org.onflow.examples.kotlin.getTransaction

import org.onflow.flow.sdk.*

class GetTransactionAccessAPIConnector(
    private val accessAPI: FlowAccessApi
) {

    fun getTransaction(txID: FlowId): FlowTransaction {
        return when (val response = accessAPI.getTransactionById(txID)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
    }

    fun getTransactionResult(txID: FlowId): FlowTransactionResult {
        return when (val response = accessAPI.getTransactionResultById(txID)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
    }
}