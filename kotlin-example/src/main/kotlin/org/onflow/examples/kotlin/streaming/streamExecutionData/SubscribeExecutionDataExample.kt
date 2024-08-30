package org.onflow.examples.kotlin.streaming.streamExecutionData

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import org.onflow.flow.sdk.*

class SubscribeExecutionDataExample(
    private val accessAPI: FlowAccessApi
) {

    suspend fun streamExecutionData(scope: CoroutineScope, receivedExecutionData: MutableList<FlowBlockExecutionData>) {
        val header: FlowBlockHeader = getLatestBlockHeader()

        val (dataChannel, errorChannel) = accessAPI.subscribeExecutionDataByBlockId(scope, header.id)

        processExecutionData(scope, dataChannel, errorChannel, receivedExecutionData)
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
        scope.launch {
            for (data in dataChannel) {
                receivedExecutionData.add(data)
            }
        }

        scope.launch {
            for (error in errorChannel) {
                println("~~~ ERROR: ${error.message} ~~~")
            }
        }
    }







    private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }
}
