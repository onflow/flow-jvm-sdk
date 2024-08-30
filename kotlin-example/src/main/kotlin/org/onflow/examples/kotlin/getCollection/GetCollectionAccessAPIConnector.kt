package org.onflow.examples.kotlin.getCollection

import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.FlowAccessApi.AccessApiCallResponse

class GetCollectionAccessAPIConnector(private val accessAPI: FlowAccessApi) {
    fun getCollectionById(collectionId: FlowId): FlowCollection {
        return when (val response = accessAPI.getCollectionById(collectionId)) {
            is AccessApiCallResponse.Success -> response.data
            is AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
    }
}