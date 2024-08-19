package org.onflow.examples.kotlin.getNetworkParams

import org.onflow.flow.sdk.FlowAccessApi
import org.onflow.flow.sdk.FlowChainId

internal class GetNetworkParametersAccessAPIConnector(
    private val accessAPI: FlowAccessApi
) {
    fun getNetworkParameters(): FlowChainId {
        return when (val response = accessAPI.getNetworkParameters()) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
    }
}
