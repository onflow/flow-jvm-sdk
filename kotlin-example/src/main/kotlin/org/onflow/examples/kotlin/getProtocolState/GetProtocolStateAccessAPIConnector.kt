package org.onflow.examples.kotlin.getProtocolState

import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.FlowId
import org.onflow.flow.sdk.FlowSnapshot

internal class GetProtocolStateAccessAPIConnector(
    private val accessAPI: FlowAccessApi
) {
    fun getLatestProtocolStateSnapshot(): FlowSnapshot =
        when (val response = accessAPI.getLatestProtocolStateSnapshot()) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

    fun getProtocolStateSnapshotByBlockId(blockId: FlowId): FlowSnapshot =
        when (val response = accessAPI.getProtocolStateSnapshotByBlockId(blockId)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }

    fun getProtocolStateSnapshotByHeight(height: Long): FlowSnapshot =
        when (val response = accessAPI.getProtocolStateSnapshotByHeight(height)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
}
