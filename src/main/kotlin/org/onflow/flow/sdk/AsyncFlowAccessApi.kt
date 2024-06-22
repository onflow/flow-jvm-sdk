package org.onflow.flow.sdk

import com.google.protobuf.ByteString
import java.util.concurrent.CompletableFuture

interface AsyncFlowAccessApi {
    fun ping(): CompletableFuture<FlowAccessApi.FlowResult<Unit>>

    fun getLatestBlockHeader(sealed: Boolean = true): CompletableFuture<FlowAccessApi.FlowResult<FlowBlockHeader>>

    fun getBlockHeaderById(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<FlowBlockHeader?>>

    fun getBlockHeaderByHeight(height: Long): CompletableFuture<FlowAccessApi.FlowResult<FlowBlockHeader?>>

    fun getLatestBlock(sealed: Boolean = true): CompletableFuture<FlowAccessApi.FlowResult<FlowBlock>>

    fun getBlockById(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<FlowBlock?>>

    fun getBlockByHeight(height: Long): CompletableFuture<FlowAccessApi.FlowResult<FlowBlock?>>

    fun getTransactionsByBlockId(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<List<FlowTransaction>>>

    fun getTransactionResultsByBlockId(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<List<FlowTransactionResult>>>

    fun getExecutionResultByBlockId(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<ExecutionResult?>>

    fun getCollectionById(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<FlowCollection?>>

    fun sendTransaction(transaction: FlowTransaction): CompletableFuture<FlowAccessApi.FlowResult<FlowId>>

    fun getTransactionById(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<FlowTransaction?>>

    fun getTransactionResultById(id: FlowId): CompletableFuture<FlowAccessApi.FlowResult<FlowTransactionResult?>>

    @Deprecated(
        message = "Behaves identically to getAccountAtLatestBlock",
        replaceWith = ReplaceWith("getAccountAtLatestBlock")
    )
    fun getAccountByAddress(addresss: FlowAddress): CompletableFuture<FlowAccessApi.FlowResult<FlowAccount>>

    fun getAccountAtLatestBlock(addresss: FlowAddress): CompletableFuture<FlowAccessApi.FlowResult<FlowAccount>>

    fun getAccountByBlockHeight(addresss: FlowAddress, height: Long): CompletableFuture<FlowAccessApi.FlowResult<FlowAccount>>

    fun executeScriptAtLatestBlock(script: FlowScript, arguments: Iterable<ByteString> = emptyList()): CompletableFuture<FlowAccessApi.FlowResult<FlowScriptResponse>>

    fun executeScriptAtBlockId(script: FlowScript, blockId: FlowId, arguments: Iterable<ByteString> = emptyList()): CompletableFuture<FlowAccessApi.FlowResult<FlowScriptResponse>>

    fun executeScriptAtBlockHeight(script: FlowScript, height: Long, arguments: Iterable<ByteString> = emptyList()): CompletableFuture<FlowAccessApi.FlowResult<FlowScriptResponse>>

    fun getEventsForHeightRange(type: String, range: ClosedRange<Long>): CompletableFuture<FlowAccessApi.FlowResult<List<FlowEventResult>>>

    fun getEventsForBlockIds(type: String, ids: Set<FlowId>): CompletableFuture<FlowAccessApi.FlowResult<List<FlowEventResult>>>

    fun getNetworkParameters(): CompletableFuture<FlowAccessApi.FlowResult<FlowChainId>>

    fun getLatestProtocolStateSnapshot(): CompletableFuture<FlowAccessApi.FlowResult<FlowSnapshot>>
}
