package org.onflow.examples.kotlin.streaming.streamExecutionData

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import org.onflow.flow.sdk.*

class SubscribeExecutionDataExample(
    private val accessAPI: FlowAccessApi
) {
    suspend fun streamExecutionData(
        scope: CoroutineScope,
        receivedExecutionData: MutableList<FlowBlockExecutionData>
    ) {
        val header: FlowBlockHeader = getLatestBlockHeader()

        val (dataChannel, errorChannel, job) = accessAPI.subscribeExecutionDataByBlockId(scope, header.id)
        processExecutionData(scope, dataChannel, errorChannel, receivedExecutionData)
        job.cancelAndJoin()
    }

    private fun getLatestBlockHeader(): FlowBlockHeader {
        return when (val response = accessAPI.getLatestBlockHeader(true)) {
            is FlowAccessApi.AccessApiCallResponse.Success -> response.data
            is FlowAccessApi.AccessApiCallResponse.Error -> throw Exception(response.message, response.throwable)
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
                    if (!isActive) break
                    receivedExecutionData.add(data)
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

