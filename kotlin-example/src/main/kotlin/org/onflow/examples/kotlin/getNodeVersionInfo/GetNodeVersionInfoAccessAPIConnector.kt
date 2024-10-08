package org.onflow.examples.kotlin.getNodeVersionInfo

import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.FlowNodeVersionInfo

internal class GetNodeVersionInfoAccessAPIConnector(
    private val accessAPI: FlowAccessApi
) {
    fun getNodeVersionInfo(): FlowNodeVersionInfo {
        return when (val response = accessAPI.getNodeVersionInfo()) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
    }
}
