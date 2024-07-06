package org.onflow.flow.sdk

import com.google.protobuf.ByteString
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.channels.ReceiveChannel
import org.onflow.protobuf.executiondata.Executiondata

interface FlowAccessApi {
    sealed class AccessApiCallResponse<out T> {
        data class Success<out T>(val data: T) : AccessApiCallResponse<T>()
        data class Error(val message: String, val throwable: Throwable? = null) : AccessApiCallResponse<Nothing>()
    }

    fun ping(): AccessApiCallResponse<Unit>

    fun getLatestBlockHeader(sealed: Boolean = true): AccessApiCallResponse<FlowBlockHeader>

    fun getBlockHeaderById(id: FlowId): AccessApiCallResponse<FlowBlockHeader>

    fun getBlockHeaderByHeight(height: Long): AccessApiCallResponse<FlowBlockHeader>

    fun getLatestBlock(sealed: Boolean = true): AccessApiCallResponse<FlowBlock>

    fun getBlockById(id: FlowId): AccessApiCallResponse<FlowBlock>

    fun getBlockByHeight(height: Long): AccessApiCallResponse<FlowBlock>

    fun getCollectionById(id: FlowId): AccessApiCallResponse<FlowCollection>

    fun sendTransaction(transaction: FlowTransaction): AccessApiCallResponse<FlowId>

    fun getTransactionById(id: FlowId): AccessApiCallResponse<FlowTransaction>

    fun getTransactionResultById(id: FlowId): AccessApiCallResponse<FlowTransactionResult>

    @Deprecated(
        message = "Behaves identically to getAccountAtLatestBlock",
        replaceWith = ReplaceWith("getAccountAtLatestBlock")
    )
    fun getAccountByAddress(addresss: FlowAddress): AccessApiCallResponse<FlowAccount>

    fun getAccountAtLatestBlock(addresss: FlowAddress): AccessApiCallResponse<FlowAccount>

    fun getAccountByBlockHeight(addresss: FlowAddress, height: Long): AccessApiCallResponse<FlowAccount>

    fun executeScriptAtLatestBlock(script: FlowScript, arguments: Iterable<ByteString> = emptyList()): AccessApiCallResponse<FlowScriptResponse>

    fun executeScriptAtBlockId(script: FlowScript, blockId: FlowId, arguments: Iterable<ByteString> = emptyList()): AccessApiCallResponse<FlowScriptResponse>

    fun executeScriptAtBlockHeight(script: FlowScript, height: Long, arguments: Iterable<ByteString> = emptyList()): AccessApiCallResponse<FlowScriptResponse>

    fun getEventsForHeightRange(type: String, range: ClosedRange<Long>): AccessApiCallResponse<List<FlowEventResult>>

    fun getEventsForBlockIds(type: String, ids: Set<FlowId>): AccessApiCallResponse<List<FlowEventResult>>

    fun getNetworkParameters(): AccessApiCallResponse<FlowChainId>

    fun getLatestProtocolStateSnapshot(): AccessApiCallResponse<FlowSnapshot>

    fun getTransactionsByBlockId(id: FlowId): AccessApiCallResponse<List<FlowTransaction>>

    fun getTransactionResultsByBlockId(id: FlowId): AccessApiCallResponse<List<FlowTransactionResult>>

    fun getExecutionResultByBlockId(id: FlowId): AccessApiCallResponse<FlowExecutionResult>

    fun subscribeExecutionDataByBlockId(
        blockId: FlowId
    ): AccessApiCallResponse<Pair<ReceiveChannel<Executiondata.SubscribeExecutionDataResponse>, ReceiveChannel<Throwable>>>

    fun subscribeExecutionDataByBlockHeight(
        height: Long
    ): AccessApiCallResponse<Pair<ReceiveChannel<Executiondata.SubscribeExecutionDataResponse>, ReceiveChannel<Throwable>>>

    fun subscribeEventsByBlockId(
        blockId: FlowId
    ): AccessApiCallResponse<Pair<ReceiveChannel<List<FlowEvent>>, ReceiveChannel<Throwable>>>

    fun subscribeEventsByBlockHeight(
        height: Long
    ): AccessApiCallResponse<Pair<ReceiveChannel<List<FlowEvent>>, ReceiveChannel<Throwable>>>

}
