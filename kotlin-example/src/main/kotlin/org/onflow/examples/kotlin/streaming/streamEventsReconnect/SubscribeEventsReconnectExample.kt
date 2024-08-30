package org.onflow.examples.kotlin.streaming.streamEventsReconnect

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.selects.select
import org.onflow.flow.sdk.*

class SubscribeEventsReconnectExample(
    accessApiConnection: FlowAccessApi
) {
    private val accessAPI = accessApiConnection

    suspend fun streamEvents(scope: CoroutineScope, receivedEvents: MutableList<FlowEvent>) {
        val header: FlowBlockHeader = getLatestBlockHeader()

        val (eventChannel, errorChannel) = accessAPI.subscribeEventsByBlockId(scope, header.id)

        val lastHeight = header.height

        scope.launch {
            handleEventStream(scope, eventChannel, errorChannel, lastHeight, receivedEvents)
        }
    }

    private fun getLatestBlockHeader(): FlowBlockHeader {
        return when (val response = accessAPI.getLatestBlockHeader(true)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun handleEventStream(
        scope: CoroutineScope,
        initialEventChannel: ReceiveChannel<List<FlowEvent>>,
        initialErrorChannel: ReceiveChannel<Throwable>,
        lastHeight: Long,
        receivedEvents: MutableList<FlowEvent>
    ) {
        var eventChannel = initialEventChannel
        var errorChannel = initialErrorChannel
        var height = lastHeight
        var reconnectAttempts = 0
        val maxReconnectAttempts = 5

        while (reconnectAttempts < maxReconnectAttempts) {
            val shouldReconnect: Boolean = select {
                eventChannel.onReceiveCatching { result ->
                    result.getOrNull()?.let { events ->
                        receivedEvents.addAll(events)
                        println("Received events at height: $height")
                        height++
                        reconnectAttempts = 0 // Reset reconnect attempts on success
                        false
                    } ?: run {
                        println("No events received, attempting to reconnect...")
                        true
                    }
                }
                errorChannel.onReceiveCatching { result ->
                    result.getOrNull()?.let { error ->
                        println("~~~ ERROR: ${error.message} ~~~")
                        true
                    } ?: run {
                        true
                    }
                }
                onTimeout(1000L) {
                    println("Timeout occurred, checking channels...")
                    false
                }
            }

            if (shouldReconnect) {
                reconnectAttempts++
                if (reconnectAttempts < maxReconnectAttempts) {
                    println("Reconnecting at block $height (attempt $reconnectAttempts/$maxReconnectAttempts)")
                    reconnect(scope, height).let {
                        eventChannel = it.first
                        errorChannel = it.second
                    }
                } else {
                    println("Max reconnect attempts reached. Stopping.")
                    break
                }
            }
        }
        println("Event streaming stopped.")
    }

    private fun reconnect(
        scope: CoroutineScope,
        height: Long
    ): Pair<ReceiveChannel<List<FlowEvent>>, ReceiveChannel<Throwable>> {
        return accessAPI.subscribeEventsByBlockHeight(scope, height)
    }
}
