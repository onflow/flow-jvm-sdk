package org.onflow.examples.kotlin.streaming.streamExecutionData

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import org.onflow.examples.kotlin.AccessAPIConnector
import org.onflow.flow.sdk.*
import org.onflow.flow.sdk.crypto.PrivateKey

class SubscribeExecutionDataExample(
    privateKey: PrivateKey,
    accessApiConnection: FlowAccessApi

) {
    private val accessAPI = accessApiConnection
    private val connector = AccessAPIConnector(privateKey, accessAPI)

    suspend fun streamExecutionData(
        scope: CoroutineScope,
        receivedExecutionData: MutableList<FlowBlockExecutionData>
    ) {
        val blockId = connector.latestBlockID

        val (dataChannel, errorChannel, job) = accessAPI.subscribeExecutionDataByBlockId(scope, blockId)
        processExecutionData(scope, dataChannel, errorChannel, receivedExecutionData)
        if (job.isActive) {
            job.cancelAndJoin()
        }
    }

    private suspend fun processExecutionData(
        scope: CoroutineScope,
        dataChannel: ReceiveChannel<FlowBlockExecutionData>,
        errorChannel: ReceiveChannel<Throwable>,
        receivedExecutionData: MutableList<FlowBlockExecutionData>
    ) {
        val dataJob = scope.launch {
            try {
                for (data in dataChannel) {
                    if (!isActive) break // Respond to cancellation
                    receivedExecutionData.add(data)
                    yield() // Cooperative cancellation
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
                    if (!isActive) break // Respond to cancellation
                    println("~~~ ERROR: ${error.message} ~~~")
                    yield() // Cooperative cancellation
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

