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

        val (eventChannel, errorChannel, job) = accessAPI.subscribeEventsByBlockId(scope, header.id)
        val lastHeight = header.height

        processEventsWithReconnect(scope, eventChannel, errorChannel, lastHeight, receivedEvents, job)
    }

    private fun getLatestBlockHeader(): FlowBlockHeader {
        return when (val response = accessAPI.getLatestBlockHeader(true)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun processEventsWithReconnect(
        scope: CoroutineScope,
        initialEventChannel: ReceiveChannel<List<FlowEvent>>,
        initialErrorChannel: ReceiveChannel<Throwable>,
        lastHeight: Long,
        receivedEvents: MutableList<FlowEvent>,
        initialJob: Job
    ) {
        var eventChannel = initialEventChannel
        var errorChannel = initialErrorChannel
        var job = initialJob
        var height = lastHeight
        var reconnectAttempts = 0
        val maxReconnectAttempts = 5

        val dataJob = scope.launch {
            while (reconnectAttempts < maxReconnectAttempts) {
                val shouldReconnect: Boolean = select {
                    eventChannel.onReceiveCatching { result ->
                        result.getOrNull()?.let { events ->
                            if (events.isNotEmpty()) {
                                receivedEvents.addAll(events)
                                println("Received events at height: $height")
                                height++
                                reconnectAttempts = 0 // Reset reconnect attempts on success
                                false
                            } else {
                                println("No events received, attempting to reconnect...")
                                true
                            }
                        } ?: true
                    }
                    errorChannel.onReceiveCatching { result ->
                        result.getOrNull()?.let { error ->
                            println("~~~ ERROR: ${error.message} ~~~")
                            true
                        } == true
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

                        // Cancel the previous job before reconnecting
                        job.cancelAndJoin()

                        // Perform reconnection and update channels and job
                        val (newEventChannel, newErrorChannel, newJob) = reconnect(scope, height)
                        eventChannel = newEventChannel
                        errorChannel = newErrorChannel
                        job = newJob
                    } else {
                        println("Max reconnect attempts reached. Stopping.")
                        break
                    }
                }
            }
        }

        dataJob.join()
    }

    private fun reconnect(
        scope: CoroutineScope,
        height: Long
    ): Triple<ReceiveChannel<List<FlowEvent>>, ReceiveChannel<Throwable>, Job> {
        return accessAPI.subscribeEventsByBlockHeight(scope, height)
    }
}
