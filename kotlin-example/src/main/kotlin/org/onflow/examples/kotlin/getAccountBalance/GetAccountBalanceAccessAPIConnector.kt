package org.onflow.examples.kotlin.getAccountBalance

import org.onflow.flow.sdk.*

internal class GetBalanceAccessAPIConnector(
    private val accessAPI: FlowAccessApi
) {
    fun getBalanceAtLatestBlock(address: FlowAddress): Long {
        return when (val response = accessAPI.getAccountBalanceAtLatestBlock(address)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
    }

    fun getBalanceAtBlockHeight(address: FlowAddress, height: Long): Long {
        return when (val response = accessAPI.getAccountBalanceAtBlockHeight(address, height)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
    }
}
