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

    fun subscribeToLatestBlockEvents(scope: CoroutineScope): Pair<ReceiveChannel<List<FlowEvent>>, ReceiveChannel<Throwable>> {
        val blockId = connector.latestBlockID

        // Subscribe to events for the latest block and return channels
        return accessAPI.subscribeEventsByBlockId(scope, blockId)
    }
}
