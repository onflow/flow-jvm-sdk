package org.onflow.examples.kotlin.streaming.streamEvents

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.crypto.PrivateKey

class SubscribeEventsExample(
    privateKey: PrivateKey,
    accessApiConnection: FlowAccessApi
) {
    private val accessAPI = accessApiConnection
    private val connector = AccessAPIConnector(privateKey, accessAPI)

    suspend fun streamEvents(
        scope: CoroutineScope,
        receivedEvents: MutableList<FlowEvent>
    ) {
        val blockId = connector.latestBlockID

        val (dataChannel, errorChannel, job) = accessAPI.subscribeEventsByBlockId(scope, blockId)
        processEvents(scope, dataChannel, errorChannel, receivedEvents)
        job.cancelAndJoin()
    }
    private suspend fun processEvents(
        scope: CoroutineScope,
        dataChannel: ReceiveChannel<List<FlowEvent>>,
        errorChannel: ReceiveChannel<Throwable>,
        receivedEvents: MutableList<FlowEvent>
    ) {
        val dataJob = scope.launch {
            try {
                for (events in dataChannel) {
                    if (!isActive) break
                    if (events.isNotEmpty()) {
                        receivedEvents.addAll(events)
                    }
                    yield()
                }
            } catch (e: CancellationException) {
                println("Data channel processing cancelled")
            } finally {
                println("Data channel processing finished")
                dataChannel.cancel()
            }
        }

        val errorJob = scope.launch {
            try {
                for (error in errorChannel) {
                    println("~~~ ERROR: ${error.message} ~~~")
                    if (!isActive) break
                    yield()
                }
            } catch (e: CancellationException) {
                println("Error channel processing cancelled")
            } finally {
                println("Error channel processing finished")
                errorChannel.cancel()
            }
        }

        dataJob.join()
        errorJob.join()
    }
}
