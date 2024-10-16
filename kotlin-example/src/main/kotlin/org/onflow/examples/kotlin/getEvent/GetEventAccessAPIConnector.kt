package org.onflow.examples.kotlin.getEvent

import org.onflow.flow.sdk.*

class GetEventAccessAPIConnector(
    private val accessAPI: FlowAccessApi
) {
    fun getEventsForHeightRange(eventType: String, startHeight: Long, endHeight: Long): List<FlowEventResult> =
        when (val response = accessAPI.getEventsForHeightRange(eventType, startHeight..endHeight)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw RuntimeException(response.message, response.throwable)
        }

    fun getEventsForBlockIds(eventType: String, blockIds: List<FlowId>): List<FlowEventResult> =
        when (val response = accessAPI.getEventsForBlockIds(eventType, blockIds.toSet())) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw RuntimeException(response.message, response.throwable)
        }

    fun getTransactionResult(txID: FlowId): FlowTransactionResult =
        when (val response = accessAPI.getTransactionResultById(txID)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw RuntimeException(response.message, response.throwable)
        }

    fun getAccountCreatedEvents(startHeight: Long, endHeight: Long): List<FlowEventResult> =
        getEventsForHeightRange("flow.AccountCreated", startHeight, endHeight)
}
