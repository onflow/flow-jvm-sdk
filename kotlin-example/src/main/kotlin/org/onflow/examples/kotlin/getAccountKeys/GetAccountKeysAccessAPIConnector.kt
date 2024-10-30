package org.onflow.examples.kotlin.getAccountKeys

import org.onflow.flow.sdk.*

internal class GetAccountKeysAccessAPIConnector(
    private val accessAPI: FlowAccessApi
) {
    fun getAccountKeyAtLatestBlock(address: FlowAddress, keyIndex: Int): FlowAccountKey =
        when (val response = accessAPI.getAccountKeyAtLatestBlock(address, keyIndex)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

    fun getAccountKeyAtBlockHeight(address: FlowAddress, keyIndex: Int, height: Long): FlowAccountKey =
        when (val response = accessAPI.getAccountKeyAtBlockHeight(address, keyIndex, height)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

    fun getAccountKeysAtLatestBlock(address: FlowAddress): List<FlowAccountKey> =
        when (val response = accessAPI.getAccountKeysAtLatestBlock(address)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

    fun getAccountKeysAtBlockHeight(address: FlowAddress, height: Long): List<FlowAccountKey> =
        when (val response = accessAPI.getAccountKeysAtBlockHeight(address, height)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
}
