package org.onflow.flow.sdk

import com.google.protobuf.ByteString
import java.util.concurrent.CompletableFuture

interface AsyncFlowAccessApi {
    fun ping(): CompletableFuture<FlowAccessApi.AccessApiCallResponse<Unit>>

    fun getLatestBlockHeader(sealed: Boolean = true): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlockHeader>>

    fun getBlockHeaderById(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlockHeader?>>

    fun getBlockHeaderByHeight(height: Long): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlockHeader?>>

    fun getLatestBlock(sealed: Boolean = true): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlock>>

    fun getBlockById(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlock?>>

    fun getBlockByHeight(height: Long): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowBlock?>>

    fun getCollectionById(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowCollection?>>

    fun sendTransaction(transaction: FlowTransaction): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowId>>

    fun getTransactionById(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowTransaction?>>

    fun getTransactionResultById(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowTransactionResult?>>

    @Deprecated(
        message = "Behaves identically to getAccountAtLatestBlock",
        replaceWith = ReplaceWith("getAccountAtLatestBlock")
    )
    fun getAccountByAddress(addresss: FlowAddress): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowAccount>>

    fun getAccountAtLatestBlock(addresss: FlowAddress): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowAccount>>

    fun getAccountByBlockHeight(addresss: FlowAddress, height: Long): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowAccount>>

    fun executeScriptAtLatestBlock(script: FlowScript, arguments: Iterable<ByteString> = emptyList()): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowScriptResponse>>

    fun executeScriptAtBlockId(script: FlowScript, blockId: FlowId, arguments: Iterable<ByteString> = emptyList()): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowScriptResponse>>

    fun executeScriptAtBlockHeight(script: FlowScript, height: Long, arguments: Iterable<ByteString> = emptyList()): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowScriptResponse>>

    fun getEventsForHeightRange(type: String, range: ClosedRange<Long>): CompletableFuture<FlowAccessApi.AccessApiCallResponse<List<FlowEventResult>>>

    fun getEventsForBlockIds(type: String, ids: Set<FlowId>): CompletableFuture<FlowAccessApi.AccessApiCallResponse<List<FlowEventResult>>>

    fun getNetworkParameters(): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowChainId>>

    fun getLatestProtocolStateSnapshot(): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowSnapshot>>

    fun getNodeVersionInfo(): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowNodeVersionInfo>>

    fun getTransactionsByBlockId(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<List<FlowTransaction>>>

    fun getTransactionResultsByBlockId(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<List<FlowTransactionResult>>>

    fun getExecutionResultByBlockId(id: FlowId): CompletableFuture<FlowAccessApi.AccessApiCallResponse<FlowExecutionResult?>>
}
