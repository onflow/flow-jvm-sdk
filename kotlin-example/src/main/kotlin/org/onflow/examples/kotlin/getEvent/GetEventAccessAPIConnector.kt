package org.onflow.examples.kotlin.getEvent

import org.onflow.flow.sdk.*

class GetEventAccessAPIConnector(
    private val accessAPI: FlowAccessApi
) {

    fun getEventsForHeightRange(eventType: String, startHeight: Long, endHeight: Long): List<FlowEventResult> {
        val range = startHeight..endHeight
        val response = accessAPI.getEventsForHeightRange(eventType, range)
        return when (response) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw RuntimeException(response.message, response.throwable)
        }
    }

    fun getEventsForBlockIds(eventType: String, blockIds: List<FlowId>): List<FlowEventResult> {
        val blockIdSet = blockIds.toSet()
        val response = accessAPI.getEventsForBlockIds(eventType, blockIdSet)
        return when (response) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw RuntimeException(response.message, response.throwable)
        }
    }

    fun getTransactionResult(txID: FlowId): FlowTransactionResult {
        val response = accessAPI.getTransactionResultById(txID)
        return when (response) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw RuntimeException(response.message, response.throwable)
        }
    }

    fun getAccountCreatedEvents(startHeight: Long, endHeight: Long): List<FlowEventResult> {
        return getEventsForHeightRange("flow.AccountCreated", startHeight, endHeight)
    }
}
