package org.onflow.examples.kotlin.getAccount

import org.onflow.flow.sdk.*
import java.math.BigDecimal

internal class GetAccountAccessAPIConnector(
    private val accessAPI: FlowAccessApi
) {
    fun getAccountAtLatestBlock(address: FlowAddress): FlowAccount =
        when (val response = accessAPI.getAccountAtLatestBlock(address)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

    fun getAccountAtBlockHeight(address: FlowAddress, height: Long): FlowAccount =
        when (val response = accessAPI.getAccountByBlockHeight(address, height)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

    fun getAccountBalance(address: FlowAddress): BigDecimal {
        val account = getAccountAtLatestBlock(address)
        return account.balance
    }
}
